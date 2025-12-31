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
}

/**
 * Workflow status response.
 */
export interface WorkflowStatus {
  status: string;
  currentStep?: string;
  blockingReason?: string | null;
}

/**
 * Shipment state for HITL tracking.
 */
export interface ShipmentState {
  shipmentId: number;
  orderId: number;
  status: string;  // CREATED, RATE_SELECTED, LABEL_GENERATED, SHIPPED
  carrier?: string;
  serviceLevel?: string;
  trackingNumber?: string;
  labelUrl?: string;
}

/**
 * Shipment states response.
 */
export interface ShipmentStatesResponse {
  shipments: Record<number, ShipmentState>;
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
 * Fetched rates response from workflow query.
 */
export interface FetchedRatesResponse {
  shipmentId: number;
  status: string;  // PENDING, FETCHING, COMPLETED, FAILED
  rates: CarrierRate[];
  uspsStatus?: string;
  upsStatus?: string;
  fedexStatus?: string;
  errorMessage?: string;
}

/**
 * WMS (Warehouse Management System) service client.
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

  /**
   * Get all facilities.
   */
  async getFacilities(): Promise<Facility[]> {
    return this.get<Facility[]>("/facilities");
  }

  /**
   * Get a specific facility by ID.
   */
  async getFacility(id: number): Promise<Facility> {
    return this.get<Facility>(`/facilities/${id}`);
  }

  /**
   * Get all waves.
   */
  async getWaves(): Promise<Wave[]> {
    return this.get<Wave[]>("/api/waves");
  }

  /**
   * Get waves by status.
   */
  async getWavesByStatus(status: string): Promise<Wave[]> {
    return this.get<Wave[]>(`/api/waves?status=${encodeURIComponent(status)}`);
  }

  /**
   * Get waves by facility.
   */
  async getWavesByFacility(facilityId: number): Promise<Wave[]> {
    return this.get<Wave[]>(`/api/waves?facilityId=${facilityId}`);
  }

  /**
   * Get a specific wave by ID.
   */
  async getWave(id: number): Promise<Wave> {
    return this.get<Wave>(`/api/waves/${id}`);
  }

  /**
   * Create a new wave.
   */
  async createWave(request: CreateWaveRequest): Promise<Wave> {
    return this.post<Wave, CreateWaveRequest>("/api/waves", request);
  }

  /**
   * Release a wave for execution.
   * This starts the WaveExecutionWorkflow.
   */
  async releaseWave(waveId: number, request: ReleaseWaveRequest): Promise<Wave> {
    return this.post<Wave, ReleaseWaveRequest>(`/api/waves/${waveId}/release`, request);
  }

  /**
   * Signal that all picks in a wave are completed.
   */
  async signalPicksCompleted(waveId: number): Promise<void> {
    return this.post<void>(`/api/waves/${waveId}/picks-completed`);
  }

  /**
   * Signal that all packs in a wave are completed.
   */
  async signalPacksCompleted(waveId: number): Promise<void> {
    return this.post<void>(`/api/waves/${waveId}/packs-completed`);
  }

  /**
   * Cancel a wave.
   */
  async cancelWave(waveId: number, reason: string): Promise<Wave> {
    return this.delete<Wave>(`/api/waves/${waveId}?reason=${encodeURIComponent(reason)}`);
  }

  /**
   * Get workflow status for a wave.
   */
  async getWaveWorkflowStatus(waveId: number): Promise<WorkflowStatus> {
    return this.get<WorkflowStatus>(`/api/waves/${waveId}/status`);
  }

  /**
   * Get shipment states for a wave.
   */
  async getShipmentStates(waveId: number): Promise<ShipmentStatesResponse> {
    return this.get<ShipmentStatesResponse>(`/api/waves/${waveId}/shipments`);
  }

  /**
   * Signal rate selection for a shipment.
   */
  async signalRateSelected(waveId: number, shipmentId: number, carrier: string, serviceLevel: string): Promise<void> {
    return this.post<void>(`/api/waves/${waveId}/shipments/${shipmentId}/select-rate`, { carrier, serviceLevel });
  }

  /**
   * Signal to print label for a shipment.
   */
  async signalPrintLabel(waveId: number, shipmentId: number): Promise<void> {
    return this.post<void>(`/api/waves/${waveId}/shipments/${shipmentId}/print-label`);
  }

  /**
   * Signal that a shipment has been confirmed as shipped.
   */
  async signalShipmentConfirmed(waveId: number, shipmentId: number): Promise<void> {
    return this.post<void>(`/api/waves/${waveId}/shipments/${shipmentId}/confirm-shipped`);
  }

  /**
   * Signal to fetch rates for a shipment.
   * This triggers parallel rate fetching from USPS, UPS, and FedEx.
   */
  async signalFetchRates(waveId: number, shipmentId: number): Promise<void> {
    return this.post<void>(`/api/waves/${waveId}/shipments/${shipmentId}/fetch-rates`);
  }

  /**
   * Get fetched rates for a shipment.
   */
  async getFetchedRates(waveId: number, shipmentId: number): Promise<FetchedRatesResponse> {
    return this.get<FetchedRatesResponse>(`/api/waves/${waveId}/shipments/${shipmentId}/rates`);
  }

  /**
   * Get pick tasks for a wave.
   */
  async getPickTasks(waveId: number): Promise<PickTask[]> {
    return this.get<PickTask[]>(`/api/waves/${waveId}/pick-tasks`);
  }

  /**
   * Get wave counts by status for dashboard.
   */
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
