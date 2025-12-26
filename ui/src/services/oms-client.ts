import { BaseServiceClient } from "./base-client";

/**
 * Order entity from OMS.
 */
export interface Order {
  id: number;
  tenantId: string;
  externalOrderId: string;
  status: string;
  customerEmail?: string;
  customerName?: string;
  shippingAddressLine1?: string;
  shippingAddressLine2?: string;
  shippingCity?: string;
  shippingState?: string;
  shippingPostalCode?: string;
  shippingCountry?: string;
  createdByUserId?: string;
  updatedByUserId?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Order line entity from OMS.
 */
export interface OrderLine {
  id: number;
  tenantId: string;
  orderId: number;
  sku: string;
  quantity: number;
  unitPrice?: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request to create a new order.
 */
export interface CreateOrderRequest {
  externalOrderId: string;
  customerEmail?: string;
  customerName?: string;
  shippingAddressLine1?: string;
  shippingAddressLine2?: string;
  shippingCity?: string;
  shippingState?: string;
  shippingPostalCode?: string;
  shippingCountry?: string;
  lines: CreateOrderLineRequest[];
}

export interface CreateOrderLineRequest {
  sku: string;
  quantity: number;
  unitPrice?: number;
}

/**
 * OMS (Order Management System) service client.
 * Used server-side only - never expose to browser.
 */
export class OmsClient extends BaseServiceClient {
  constructor() {
    const baseUrl = process.env.OMS_URL;
    if (!baseUrl) {
      throw new Error("OMS_URL environment variable is not set");
    }
    super(baseUrl);
  }

  /**
   * Get all orders for the authenticated tenant.
   */
  async getOrders(accessToken: string): Promise<Order[]> {
    return this.get<Order[]>("/orders", accessToken);
  }

  /**
   * Get a specific order by ID.
   */
  async getOrder(id: number, accessToken: string): Promise<Order> {
    return this.get<Order>(`/orders/${id}`, accessToken);
  }

  /**
   * Create a new order.
   */
  async createOrder(
    request: CreateOrderRequest,
    accessToken: string
  ): Promise<Order> {
    return this.post<Order, CreateOrderRequest>(
      "/orders",
      accessToken,
      request
    );
  }

  /**
   * Get order lines for an order.
   */
  async getOrderLines(orderId: number, accessToken: string): Promise<OrderLine[]> {
    return this.get<OrderLine[]>(`/orders/${orderId}/lines`, accessToken);
  }
}

// Singleton instance
let omsClient: OmsClient | null = null;

/**
 * Get the OMS client instance.
 */
export function getOmsClient(): OmsClient {
  if (!omsClient) {
    omsClient = new OmsClient();
  }
  return omsClient;
}

