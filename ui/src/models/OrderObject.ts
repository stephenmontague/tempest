import { Order, OrderLine } from "@/services/oms-client";

/**
 * Order status constants.
 */
export const OrderStatus = {
  CREATED: "CREATED",
  AWAITING_WAVE: "AWAITING_WAVE",
  RESERVED: "RESERVED",
  PICKING: "PICKING",
  PICKED: "PICKED",
  PACKING: "PACKING",
  PACKED: "PACKED",
  SHIPPED: "SHIPPED",
  DELIVERED: "DELIVERED",
  CANCELLED: "CANCELLED",
  FAILED: "FAILED",
} as const;

export type OrderStatusType = (typeof OrderStatus)[keyof typeof OrderStatus];

/**
 * Domain object for Order.
 * Encapsulates business logic and derived properties.
 */
export class OrderObject {
  readonly id: number;
  readonly tenantId: string;
  readonly externalOrderId: string;
  readonly status: OrderStatusType;
  readonly customerEmail?: string;
  readonly customerName?: string;
  readonly shippingAddressLine1?: string;
  readonly shippingAddressLine2?: string;
  readonly shippingCity?: string;
  readonly shippingState?: string;
  readonly shippingPostalCode?: string;
  readonly shippingCountry?: string;
  readonly workflowId?: string;
  readonly createdByUserId?: string;
  readonly updatedByUserId?: string;
  readonly createdAt: Date;
  readonly updatedAt: Date;
  readonly lines: OrderLine[];

  private constructor(data: Order, lines: OrderLine[] = []) {
    this.id = data.id;
    this.tenantId = data.tenantId;
    this.externalOrderId = data.externalOrderId;
    this.status = data.status as OrderStatusType;
    this.customerEmail = data.customerEmail;
    this.customerName = data.customerName;
    this.shippingAddressLine1 = data.shippingAddressLine1;
    this.shippingAddressLine2 = data.shippingAddressLine2;
    this.shippingCity = data.shippingCity;
    this.shippingState = data.shippingState;
    this.shippingPostalCode = data.shippingPostalCode;
    this.shippingCountry = data.shippingCountry;
    this.workflowId = data.workflowId;
    this.createdByUserId = data.createdByUserId;
    this.updatedByUserId = data.updatedByUserId;
    this.createdAt = new Date(data.createdAt);
    this.updatedAt = new Date(data.updatedAt);
    this.lines = lines;
  }

  /**
   * Create an OrderObject from API response.
   */
  static fromJSON(data: Order, lines: OrderLine[] = []): OrderObject {
    return new OrderObject(data, lines);
  }

  /**
   * Check if the order can be edited.
   */
  isEditable(): boolean {
    return this.status === OrderStatus.CREATED;
  }

  /**
   * Check if the order can be cancelled.
   */
  canCancel(): boolean {
    const nonCancellableStatuses: OrderStatusType[] = [
      OrderStatus.SHIPPED,
      OrderStatus.DELIVERED,
      OrderStatus.CANCELLED,
      OrderStatus.FAILED,
    ];
    return !nonCancellableStatuses.includes(this.status);
  }

  /**
   * Check if the order is in a terminal state.
   */
  isTerminal(): boolean {
    const terminalStatuses: OrderStatusType[] = [
      OrderStatus.SHIPPED,
      OrderStatus.DELIVERED,
      OrderStatus.CANCELLED,
      OrderStatus.FAILED,
    ];
    return terminalStatuses.includes(this.status);
  }

  /**
   * Check if the order is awaiting wave assignment.
   */
  isAwaitingWave(): boolean {
    return this.status === OrderStatus.AWAITING_WAVE;
  }

  /**
   * Check if the order is in fulfillment.
   */
  isInFulfillment(): boolean {
    const fulfillmentStatuses: OrderStatusType[] = [
      OrderStatus.RESERVED,
      OrderStatus.PICKING,
      OrderStatus.PICKED,
      OrderStatus.PACKING,
      OrderStatus.PACKED,
    ];
    return fulfillmentStatuses.includes(this.status);
  }

  /**
   * Check if the order has a workflow running.
   */
  hasActiveWorkflow(): boolean {
    return !!this.workflowId && !this.isTerminal();
  }

  /**
   * Get the full shipping address as a formatted string.
   */
  getFormattedAddress(): string | null {
    const parts: string[] = [];

    if (this.shippingAddressLine1) parts.push(this.shippingAddressLine1);
    if (this.shippingAddressLine2) parts.push(this.shippingAddressLine2);

    const cityStateZip = [
      this.shippingCity,
      this.shippingState,
      this.shippingPostalCode,
    ]
      .filter(Boolean)
      .join(", ");

    if (cityStateZip) parts.push(cityStateZip);
    if (this.shippingCountry) parts.push(this.shippingCountry);

    return parts.length > 0 ? parts.join("\n") : null;
  }

  /**
   * Calculate the total quantity of items.
   */
  getTotalQuantity(): number {
    return this.lines.reduce((sum, line) => sum + line.quantity, 0);
  }

  /**
   * Calculate the total value of the order.
   */
  getTotalValue(): number | null {
    if (this.lines.some((line) => line.unitPrice == null)) {
      return null;
    }
    return this.lines.reduce(
      (sum, line) => sum + (line.unitPrice ?? 0) * line.quantity,
      0
    );
  }

  /**
   * Get the display name for the customer.
   */
  getCustomerDisplayName(): string {
    return this.customerName || this.customerEmail || "Unknown Customer";
  }
}

