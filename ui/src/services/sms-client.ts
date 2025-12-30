import { BaseServiceClient } from "./base-client";

/**
 * Shipment entity from SMS.
 */
export interface Shipment {
  id: number;
  tenantId: string;
  orderId: number;
  facilityId: number;
  carrier: string;
  serviceLevel: string;
  trackingNumber?: string;
  status: string;
  labelUrl?: string;
  shippedAt?: string;
  deliveredAt?: string;
  createdByUserId?: string;
  updatedByUserId?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Parcel entity from SMS.
 */
export interface Parcel {
  id: number;
  tenantId: string;
  shipmentId: number;
  trackingNumber?: string;
  weightOz?: number;
  lengthIn?: number;
  widthIn?: number;
  heightIn?: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request to create a shipment.
 */
export interface CreateShipmentRequest {
  orderId: number;
  facilityId: number;
  carrier: string;
  serviceLevel: string;
}

/**
 * SMS (Shipping Management System) service client.
 * Used server-side only - never expose to browser.
 */
export class SmsClient extends BaseServiceClient {
  constructor() {
    const baseUrl = process.env.SMS_URL;
    if (!baseUrl) {
      throw new Error("SMS_URL environment variable is not set");
    }
    super(baseUrl);
  }

  /**
   * Get all shipments.
   */
  async getShipments(): Promise<Shipment[]> {
    return this.get<Shipment[]>("/shipments");
  }

  /**
   * Get shipments by status.
   */
  async getShipmentsByStatus(status: string): Promise<Shipment[]> {
    return this.get<Shipment[]>(`/shipments?status=${encodeURIComponent(status)}`);
  }

  /**
   * Get a specific shipment by ID.
   */
  async getShipment(id: number): Promise<Shipment> {
    return this.get<Shipment>(`/shipments/${id}`);
  }

  /**
   * Create a new shipment.
   */
  async createShipment(request: CreateShipmentRequest): Promise<Shipment> {
    return this.post<Shipment, CreateShipmentRequest>("/shipments", request);
  }

  /**
   * Get shipments for an order.
   */
  async getShipmentsByOrder(orderId: number): Promise<Shipment[]> {
    return this.get<Shipment[]>(`/orders/${orderId}/shipments`);
  }

  /**
   * Get parcels for a shipment.
   */
  async getParcels(shipmentId: number): Promise<Parcel[]> {
    return this.get<Parcel[]>(`/shipments/${shipmentId}/parcels`);
  }

  /**
   * Get shipment counts by status for dashboard.
   */
  async getShipmentCounts(): Promise<Record<string, number>> {
    return this.get<Record<string, number>>("/shipments/counts");
  }
}

// Singleton instance
let smsClient: SmsClient | null = null;

/**
 * Get the SMS client instance.
 */
export function getSmsClient(): SmsClient {
  if (!smsClient) {
    smsClient = new SmsClient();
  }
  return smsClient;
}
