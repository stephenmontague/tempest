import { BaseServiceClient } from "./base-client";

/**
 * Facility entity from WMS.
 */
export interface Facility {
  id: number;
  tenantId: string;
  code: string;
  name: string;
  facilityType: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Wave entity from WMS.
 */
export interface Wave {
  id: number;
  tenantId: string;
  facilityId: number;
  waveNumber: string;
  status: string;
  orderIds: number[];
  workflowId?: string;
  createdByUserId?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Pick task entity from WMS.
 */
export interface PickTask {
  id: number;
  tenantId: string;
  waveId?: number;
  orderId: number;
  orderLineId: number;
  sku: string;
  quantity: number;
  fromLocationId?: number;
  status: string;
  assignedUserId?: string;
  pickedAt?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request to create a wave.
 */
export interface CreateWaveRequest {
  facilityId: number;
  waveNumber?: string;
  orderIds: number[];
}

/**
 * Fulfillment mode for wave execution.
 * - STANDARD: Full HITL with rate shopping, manual label, manual ship confirm
 * - EXPRESS: Skip rate shopping, use default carrier, manual label and ship confirm
 * - AUTO_SHIP: Fully automated after packing - auto label and auto ship confirm
 */
export type FulfillmentMode = "STANDARD" | "EXPRESS" | "AUTO_SHIP";

/**
 * Request to release a wave.
 */
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

/**
 * WMS (Warehouse Management System) service client.
 *
 * CRUD/system-of-record only. Wave workflow start, signals, and queries go through the
 * Temporal client (see `wave-workflow.ts`), not this REST client.
 * Used server-side only - never expose to browser.
 */
export class WmsClient extends BaseServiceClient {
  constructor() {
    const baseUrl = process.env.WMS_URL;
    if (!baseUrl) {
      throw new Error("WMS_URL environment variable is not set");
    }
    super(baseUrl);
  }

  async getFacilities(): Promise<Facility[]> {
    return this.get<Facility[]>("/facilities");
  }

  async getFacility(id: number): Promise<Facility> {
    return this.get<Facility>(`/facilities/${id}`);
  }

  async getWaves(): Promise<Wave[]> {
    return this.get<Wave[]>("/api/waves");
  }

  async getWavesByStatus(status: string): Promise<Wave[]> {
    return this.get<Wave[]>(`/api/waves?status=${encodeURIComponent(status)}`);
  }

  async getWavesByFacility(facilityId: number): Promise<Wave[]> {
    return this.get<Wave[]>(`/api/waves?facilityId=${facilityId}`);
  }

  async getWave(id: number): Promise<Wave> {
    return this.get<Wave>(`/api/waves/${id}`);
  }

  async createWave(request: CreateWaveRequest): Promise<Wave> {
    return this.post<Wave, CreateWaveRequest>("/api/waves", request);
  }

  /**
   * Release a wave (CRUD transition to RELEASED). The WaveExecutionWorkflow is started
   * separately by the Temporal client in the release server action.
   */
  async releaseWave(waveId: number, request: ReleaseWaveRequest): Promise<Wave> {
    return this.post<Wave, ReleaseWaveRequest>(`/api/waves/${waveId}/release`, request);
  }

  /**
   * Cancel a wave (CRUD transition to CANCELLED).
   */
  async cancelWave(waveId: number, reason: string): Promise<Wave> {
    return this.delete<Wave>(`/api/waves/${waveId}?reason=${encodeURIComponent(reason)}`);
  }

  async getPickTasks(waveId: number): Promise<PickTask[]> {
    return this.get<PickTask[]>(`/api/waves/${waveId}/pick-tasks`);
  }

  async getWaveCounts(): Promise<Record<string, number>> {
    return this.get<Record<string, number>>("/api/waves/counts");
  }
}

// Singleton instance
let wmsClient: WmsClient | null = null;

/**
 * Get the WMS client instance.
 */
export function getWmsClient(): WmsClient {
  if (!wmsClient) {
    wmsClient = new WmsClient();
  }
  return wmsClient;
}
