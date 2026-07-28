"use server";

import { revalidatePath } from "next/cache";

import { getTenantId } from "@/lib/auth/session";
import { getWmsClient } from "@/services/wms-client";
import {
     queryWave,
     signalWave,
     startWaveExecution,
     updateWave,
     WaveExecutionInput,
} from "@/services/wave-workflow";

export interface CreateWaveRequest {
     facilityId: number;
     waveNumber?: string;
     orderIds: number[];
}

/**
 * Fulfillment mode for wave execution.
 */
export type FulfillmentMode = "STANDARD" | "EXPRESS" | "AUTO_SHIP";

export interface ReleaseWaveRequest {
     orders: {
          orderId: number;
          externalOrderId: string;
          orderLines: {
               orderLineId: number;
               sku: string;
               quantity: number;
          }[];
          shipTo?: {
               name?: string;
               addressLine1?: string;
               addressLine2?: string;
               city?: string;
               state?: string;
               postalCode?: string;
               country?: string;
          };
     }[];
     /** Fulfillment mode - defaults to STANDARD if not specified */
     fulfillmentMode?: FulfillmentMode;
     /** Default carrier for EXPRESS and AUTO_SHIP modes */
     defaultCarrier?: string;
     /** Default service level for EXPRESS and AUTO_SHIP modes */
     defaultServiceLevel?: string;
}

export interface ActionResult<T = void> {
     success: boolean;
     data?: T;
     error?: string;
}

/**
 * Create a new wave with the specified orders. Pure CRUD (no workflow).
 */
export async function createWave(
     request: CreateWaveRequest
): Promise<ActionResult<{ waveId: number; waveNumber: string }>> {
     try {
          const client = getWmsClient();

          const wave = await client.createWave(request);

          revalidatePath("/waves");
          revalidatePath(`/waves/${wave.id}`);

          return {
               success: true,
               data: {
                    waveId: wave.id,
                    waveNumber: wave.waveNumber,
               },
          };
     } catch (error) {
          console.error("Failed to create wave:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to create wave",
          };
     }
}

/**
 * Release a wave for execution.
 *
 * The UI is the Temporal client: WMS performs the CRUD transition (CREATED -> RELEASED
 * and records the deterministic workflow id), then we start the WaveExecutionWorkflow
 * directly via the Temporal client with the input assembled from the wave + request.
 */
export async function releaseWave(
     waveId: number,
     request: ReleaseWaveRequest
): Promise<ActionResult<{ workflowId: string }>> {
     try {
          const client = getWmsClient();
          const tenantId = await getTenantId();

          // Read wave (facilityId, waveNumber) then perform the CRUD release.
          const wave = await client.getWave(waveId);
          await client.releaseWave(waveId, request);

          const input: WaveExecutionInput = {
               tenantId,
               waveId,
               facilityId: wave.facilityId,
               waveNumber: wave.waveNumber,
               orders: request.orders.map((o) => ({
                    orderId: o.orderId,
                    externalOrderId: o.externalOrderId,
                    orderLines: o.orderLines.map((l) => ({
                         orderLineId: l.orderLineId,
                         sku: l.sku,
                         quantity: l.quantity,
                    })),
                    shipTo: o.shipTo ?? null,
               })),
               fulfillmentMode: request.fulfillmentMode ?? "STANDARD",
               defaultCarrier: request.defaultCarrier,
               defaultServiceLevel: request.defaultServiceLevel,
          };

          const workflowId = await startWaveExecution(input);

          revalidatePath("/waves");
          revalidatePath(`/waves/${waveId}`);
          revalidatePath("/orders");

          return {
               success: true,
               data: { workflowId },
          };
     } catch (error) {
          console.error("Failed to release wave:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to release wave",
          };
     }
}

/**
 * Signal that all picks in a wave are completed.
 */
export async function signalPicksComplete(waveId: number): Promise<ActionResult> {
     try {
          await signalWave(waveId, "allPicksCompleted");

          revalidatePath(`/waves/${waveId}`);
          revalidatePath("/waves");

          return { success: true };
     } catch (error) {
          console.error("Failed to signal picks complete:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to signal picks complete",
          };
     }
}

/**
 * Signal that all packs in a wave are completed.
 */
export async function signalPacksComplete(waveId: number): Promise<ActionResult> {
     try {
          await signalWave(waveId, "allPacksCompleted");

          revalidatePath(`/waves/${waveId}`);
          revalidatePath("/waves");
          revalidatePath("/shipments");

          return { success: true };
     } catch (error) {
          console.error("Failed to signal packs complete:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to signal packs complete",
          };
     }
}

/**
 * Cancel a wave: signal the workflow to compensate (if running), then CRUD-cancel in WMS.
 */
export async function cancelWave(waveId: number, reason: string): Promise<ActionResult> {
     try {
          const client = getWmsClient();

          // Best-effort cancel signal (the workflow may not be running yet).
          try {
               await signalWave(waveId, "cancelWave", reason);
          } catch (signalError) {
               console.warn("cancelWave signal skipped:", signalError);
          }

          await client.cancelWave(waveId, reason);

          revalidatePath("/waves");
          revalidatePath(`/waves/${waveId}`);
          revalidatePath("/orders");

          return { success: true };
     } catch (error) {
          console.error("Failed to cancel wave:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to cancel wave",
          };
     }
}

/**
 * Get the workflow status for a wave: DB status (WMS) merged with live workflow queries.
 */
export async function getWaveWorkflowStatus(
     waveId: number
): Promise<ActionResult<{ status: string; currentStep?: string; blockingReason?: string | null }>> {
     try {
          const client = getWmsClient();
          const wave = await client.getWave(waveId);

          let currentStep: string | undefined;
          let blockingReason: string | null | undefined;

          if (wave.workflowId) {
               try {
                    currentStep = await queryWave<string>(waveId, "getCurrentStep");
                    blockingReason = await queryWave<string | null>(waveId, "getBlockingReason");
               } catch {
                    // Workflow completed / not found - fall back to DB status only.
               }
          }

          return {
               success: true,
               data: { status: wave.status, currentStep, blockingReason },
          };
     } catch (error) {
          console.error("Failed to get wave workflow status:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to get workflow status",
          };
     }
}

/**
 * Shipment state interface for UI.
 */
export interface ShipmentState {
     shipmentId: number;
     orderId: number;
     status: string;
     carrier?: string;
     serviceLevel?: string;
     trackingNumber?: string;
     labelUrl?: string;
}

/**
 * Get shipment states for a wave (workflow query).
 */
export async function getShipmentStates(waveId: number): Promise<ActionResult<Record<number, ShipmentState>>> {
     try {
          const shipments = await queryWave<Record<number, ShipmentState>>(waveId, "getShipmentStates");

          return {
               success: true,
               data: shipments ?? {},
          };
     } catch (error) {
          console.error("Failed to get shipment states:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to get shipment states",
          };
     }
}

/**
 * Signal rate selection for a shipment.
 */
export async function signalRateSelected(
     waveId: number,
     shipmentId: number,
     carrier: string,
     serviceLevel: string
): Promise<ActionResult<ShipmentState>> {
     try {
          // Request-response via Temporal Update: the workflow validates the request and
          // returns the updated shipment state synchronously (positional args match the
          // Java @UpdateMethod rateSelected(Long, String, String)).
          const updated = await updateWave<ShipmentState>(
               waveId,
               "rateSelected",
               shipmentId,
               carrier,
               serviceLevel
          );

          revalidatePath(`/waves/${waveId}`);

          return { success: true, data: updated };
     } catch (error) {
          console.error("Failed to select rate:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to select rate",
          };
     }
}

/**
 * Signal to print label for a shipment.
 */
export async function signalPrintLabel(waveId: number, shipmentId: number): Promise<ActionResult> {
     try {
          await signalWave(waveId, "printLabel", shipmentId);

          revalidatePath(`/waves/${waveId}`);

          return { success: true };
     } catch (error) {
          console.error("Failed to signal print label:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to print label",
          };
     }
}

/**
 * Signal that a shipment has been confirmed as shipped.
 */
export async function signalShipmentConfirmed(waveId: number, shipmentId: number): Promise<ActionResult> {
     try {
          await signalWave(waveId, "shipmentConfirmed", shipmentId);

          revalidatePath(`/waves/${waveId}`);
          revalidatePath("/shipments");

          return { success: true };
     } catch (error) {
          console.error("Failed to signal shipment confirmed:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to confirm shipment",
          };
     }
}

/**
 * Carrier rate from rate shopping.
 */
export interface CarrierRate {
     carrier: string;
     serviceLevel: string;
     price: number;
     estimatedDelivery: string;
}

/**
 * Fetched rates state.
 */
export interface FetchedRatesState {
     shipmentId: number;
     status: string;
     rates: CarrierRate[];
     uspsStatus?: string;
     upsStatus?: string;
     fedexStatus?: string;
     errorMessage?: string;
}

/**
 * Signal to fetch rates for a shipment (parallel USPS/UPS/FedEx fetch in the workflow).
 */
export async function signalFetchRates(waveId: number, shipmentId: number): Promise<ActionResult> {
     try {
          await signalWave(waveId, "fetchRates", shipmentId);

          return { success: true };
     } catch (error) {
          console.error("Failed to signal fetch rates:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to fetch rates",
          };
     }
}

/**
 * Get fetched rates for a shipment (workflow query).
 */
export async function getFetchedRates(
     waveId: number,
     shipmentId: number
): Promise<ActionResult<FetchedRatesState>> {
     try {
          const rates = await queryWave<FetchedRatesState>(waveId, "getFetchedRates", shipmentId);

          return {
               success: true,
               data: rates,
          };
     } catch (error) {
          console.error("Failed to get fetched rates:", error);
          return {
               success: false,
               error: error instanceof Error ? error.message : "Failed to get fetched rates",
          };
     }
}
