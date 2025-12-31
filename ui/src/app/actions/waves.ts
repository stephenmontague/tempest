"use server";

import { revalidatePath } from "next/cache";
import { getWmsClient } from "@/services/wms-client";

export interface CreateWaveRequest {
     facilityId: number;
     waveNumber?: string;
     orderIds: number[];
}

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
}

export interface ActionResult<T = void> {
     success: boolean;
     data?: T;
     error?: string;
}

/**
 * Create a new wave with the specified orders.
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
 * This starts the WaveExecutionWorkflow.
 */
export async function releaseWave(
     waveId: number,
     request: ReleaseWaveRequest
): Promise<ActionResult<{ workflowId: string }>> {
     try {
          const client = getWmsClient();

          const wave = await client.releaseWave(waveId, request);

          revalidatePath("/waves");
          revalidatePath(`/waves/${waveId}`);
          revalidatePath("/orders");

          return {
               success: true,
               data: {
                    workflowId: wave.workflowId ?? `wave-execution-${waveId}`,
               },
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
          const client = getWmsClient();

          await client.signalPicksCompleted(waveId);

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
          const client = getWmsClient();

          await client.signalPacksCompleted(waveId);

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
 * Cancel a wave.
 */
export async function cancelWave(waveId: number, reason: string): Promise<ActionResult> {
     try {
          const client = getWmsClient();

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
 * Get the workflow status for a wave.
 */
export async function getWaveWorkflowStatus(
     waveId: number
): Promise<ActionResult<{ status: string; currentStep?: string; blockingReason?: string | null }>> {
     try {
          const client = getWmsClient();

          const status = await client.getWaveWorkflowStatus(waveId);

          return {
               success: true,
               data: status,
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
 * Get shipment states for a wave.
 */
export async function getShipmentStates(waveId: number): Promise<ActionResult<Record<number, ShipmentState>>> {
     try {
          const client = getWmsClient();

          const response = await client.getShipmentStates(waveId);

          return {
               success: true,
               data: response.shipments,
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
): Promise<ActionResult> {
     try {
          const client = getWmsClient();

          await client.signalRateSelected(waveId, shipmentId, carrier, serviceLevel);

          revalidatePath(`/waves/${waveId}`);

          return { success: true };
     } catch (error) {
          console.error("Failed to signal rate selected:", error);
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
          const client = getWmsClient();

          await client.signalPrintLabel(waveId, shipmentId);

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
          const client = getWmsClient();

          await client.signalShipmentConfirmed(waveId, shipmentId);

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
 * Signal to fetch rates for a shipment.
 * This triggers parallel rate fetching from USPS, UPS, and FedEx.
 */
export async function signalFetchRates(waveId: number, shipmentId: number): Promise<ActionResult> {
     try {
          const client = getWmsClient();

          await client.signalFetchRates(waveId, shipmentId);

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
 * Get fetched rates for a shipment.
 */
export async function getFetchedRates(
     waveId: number,
     shipmentId: number
): Promise<ActionResult<FetchedRatesState>> {
     try {
          const client = getWmsClient();

          const rates = await client.getFetchedRates(waveId, shipmentId);

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
