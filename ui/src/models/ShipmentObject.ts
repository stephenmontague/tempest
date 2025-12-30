import { Shipment, Parcel } from "@/services/sms-client";

/**
 * Shipment status constants.
 */
export const ShipmentStatus = {
  PENDING: "PENDING",
  LABEL_CREATED: "LABEL_CREATED",
  IN_TRANSIT: "IN_TRANSIT",
  DELIVERED: "DELIVERED",
  FAILED: "FAILED",
} as const;

export type ShipmentStatusType = (typeof ShipmentStatus)[keyof typeof ShipmentStatus];

/**
 * Domain object for Shipment.
 * Encapsulates business logic and derived properties.
 */
export class ShipmentObject {
  readonly id: number;
  readonly tenantId: string;
  readonly orderId: number;
  readonly facilityId: number;
  readonly carrier: string;
  readonly serviceLevel: string;
  readonly trackingNumber?: string;
  readonly status: ShipmentStatusType;
  readonly labelUrl?: string;
  readonly shippedAt?: Date;
  readonly deliveredAt?: Date;
  readonly createdByUserId?: string;
  readonly updatedByUserId?: string;
  readonly createdAt: Date;
  readonly updatedAt: Date;
  readonly parcels: Parcel[];

  private constructor(data: Shipment, parcels: Parcel[] = []) {
    this.id = data.id;
    this.tenantId = data.tenantId;
    this.orderId = data.orderId;
    this.facilityId = data.facilityId;
    this.carrier = data.carrier;
    this.serviceLevel = data.serviceLevel;
    this.trackingNumber = data.trackingNumber;
    this.status = data.status as ShipmentStatusType;
    this.labelUrl = data.labelUrl;
    this.shippedAt = data.shippedAt ? new Date(data.shippedAt) : undefined;
    this.deliveredAt = data.deliveredAt ? new Date(data.deliveredAt) : undefined;
    this.createdByUserId = data.createdByUserId;
    this.updatedByUserId = data.updatedByUserId;
    this.createdAt = new Date(data.createdAt);
    this.updatedAt = new Date(data.updatedAt);
    this.parcels = parcels;
  }

  /**
   * Create a ShipmentObject from API response.
   */
  static fromJSON(data: Shipment, parcels: Parcel[] = []): ShipmentObject {
    return new ShipmentObject(data, parcels);
  }

  /**
   * Check if the shipment has been shipped.
   */
  isShipped(): boolean {
    return !!this.shippedAt;
  }

  /**
   * Check if the shipment has been delivered.
   */
  isDelivered(): boolean {
    return this.status === ShipmentStatus.DELIVERED;
  }

  /**
   * Check if the shipment is in transit.
   */
  isInTransit(): boolean {
    return this.status === ShipmentStatus.IN_TRANSIT;
  }

  /**
   * Check if the shipment is in a terminal state.
   */
  isTerminal(): boolean {
    return (
      this.status === ShipmentStatus.DELIVERED ||
      this.status === ShipmentStatus.FAILED
    );
  }

  /**
   * Check if tracking is available.
   */
  hasTracking(): boolean {
    return !!this.trackingNumber;
  }

  /**
   * Check if a label is available.
   */
  hasLabel(): boolean {
    return !!this.labelUrl;
  }

  /**
   * Get the carrier and service display.
   */
  getCarrierDisplay(): string {
    return `${this.carrier} ${this.serviceLevel}`;
  }

  /**
   * Get the tracking URL (if carrier supports it).
   */
  getTrackingUrl(): string | null {
    if (!this.trackingNumber) return null;

    // Common carrier tracking URLs
    const carrierUrls: Record<string, string> = {
      UPS: `https://www.ups.com/track?tracknum=${this.trackingNumber}`,
      FEDEX: `https://www.fedex.com/fedextrack/?trknbr=${this.trackingNumber}`,
      USPS: `https://tools.usps.com/go/TrackConfirmAction?tLabels=${this.trackingNumber}`,
      DHL: `https://www.dhl.com/en/express/tracking.html?AWB=${this.trackingNumber}`,
    };

    return carrierUrls[this.carrier.toUpperCase()] ?? null;
  }

  /**
   * Get the parcel count.
   */
  getParcelCount(): number {
    return this.parcels.length;
  }

  /**
   * Get the total weight of all parcels.
   */
  getTotalWeight(): number | null {
    if (this.parcels.some((p) => p.weightOz == null)) {
      return null;
    }
    return this.parcels.reduce((sum, p) => sum + (p.weightOz ?? 0), 0);
  }

  /**
   * Get the transit time in days (if delivered).
   */
  getTransitDays(): number | null {
    if (!this.shippedAt || !this.deliveredAt) return null;

    const diffMs = this.deliveredAt.getTime() - this.shippedAt.getTime();
    return Math.ceil(diffMs / (1000 * 60 * 60 * 24));
  }
}

