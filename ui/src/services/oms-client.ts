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
  workflowId?: string;
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
 * Response from creating an order.
 * Returns the created order info after workflow completes.
 */
export interface CreateOrderResponse {
  orderId: number;
  status: string;
  externalOrderId: string;
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
   * Get all orders.
   */
  async getOrders(): Promise<Order[]> {
    return this.get<Order[]>("/orders");
  }

  /**
   * Get orders by status.
   */
  async getOrdersByStatus(status: string): Promise<Order[]> {
    return this.get<Order[]>(`/orders?status=${encodeURIComponent(status)}`);
  }

  /**
   * Get orders containing a specific SKU.
   */
  async getOrdersBySku(sku: string): Promise<Order[]> {
    return this.get<Order[]>(`/orders?sku=${encodeURIComponent(sku)}`);
  }

  /**
   * Get a specific order by ID.
   */
  async getOrder(id: number): Promise<Order> {
    return this.get<Order>(`/orders/${id}`);
  }

  /**
   * Create a new order.
   * This triggers the OrderIntakeWorkflow.
   * Returns workflow info since order creation is async.
   */
  async createOrder(request: CreateOrderRequest): Promise<CreateOrderResponse> {
    return this.post<CreateOrderResponse, CreateOrderRequest>("/orders", request);
  }

  /**
   * Get order lines for an order.
   */
  async getOrderLines(orderId: number): Promise<OrderLine[]> {
    return this.get<OrderLine[]>(`/orders/${orderId}/lines`);
  }

  /**
   * Get workflow status for an order.
   */
  async getOrderWorkflowStatus(workflowId: string): Promise<WorkflowStatus> {
    return this.get<WorkflowStatus>(`/workflows/${workflowId}/status`);
  }

  /**
   * Cancel an order.
   */
  async cancelOrder(orderId: number, reason: string): Promise<void> {
    return this.post<void, { reason: string }>(`/orders/${orderId}/cancel`, { reason });
  }

  /**
   * Get order counts by status for dashboard.
   */
  async getOrderCounts(): Promise<Record<string, number>> {
    return this.get<Record<string, number>>("/orders/counts");
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
