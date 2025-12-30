import { Wave } from "@/services/wms-client";

/**
 * Wave status constants.
 */
export const WaveStatus = {
  CREATED: "CREATED",
  RELEASED: "RELEASED",
  IN_PROGRESS: "IN_PROGRESS",
  COMPLETED: "COMPLETED",
  CANCELLED: "CANCELLED",
  FAILED: "FAILED",
} as const;

export type WaveStatusType = (typeof WaveStatus)[keyof typeof WaveStatus];

/**
 * Domain object for Wave.
 * Encapsulates business logic and derived properties.
 */
export class WaveObject {
  readonly id: number;
  readonly tenantId: string;
  readonly facilityId: number;
  readonly waveNumber: string;
  readonly status: WaveStatusType;
  readonly orderIds: number[];
  readonly workflowId?: string;
  readonly createdByUserId?: string;
  readonly createdAt: Date;
  readonly updatedAt: Date;

  private constructor(data: Wave) {
    this.id = data.id;
    this.tenantId = data.tenantId;
    this.facilityId = data.facilityId;
    this.waveNumber = data.waveNumber;
    this.status = data.status as WaveStatusType;
    this.orderIds = data.orderIds ?? [];
    this.workflowId = data.workflowId;
    this.createdByUserId = data.createdByUserId;
    this.createdAt = new Date(data.createdAt);
    this.updatedAt = new Date(data.updatedAt);
  }

  /**
   * Create a WaveObject from API response.
   */
  static fromJSON(data: Wave): WaveObject {
    return new WaveObject(data);
  }

  /**
   * Check if the wave can be released.
   */
  canRelease(): boolean {
    return this.status === WaveStatus.CREATED && this.orderIds.length > 0;
  }

  /**
   * Check if picks can be signaled as complete.
   */
  canSignalPicks(): boolean {
    return (
      this.status === WaveStatus.RELEASED ||
      this.status === WaveStatus.IN_PROGRESS
    );
  }

  /**
   * Check if packs can be signaled as complete.
   */
  canSignalPacks(): boolean {
    return this.status === WaveStatus.IN_PROGRESS;
  }

  /**
   * Check if the wave can be cancelled.
   */
  canCancel(): boolean {
    return (
      this.status !== WaveStatus.COMPLETED &&
      this.status !== WaveStatus.CANCELLED
    );
  }

  /**
   * Check if the wave is in a terminal state.
   */
  isTerminal(): boolean {
    return (
      this.status === WaveStatus.COMPLETED ||
      this.status === WaveStatus.CANCELLED ||
      this.status === WaveStatus.FAILED
    );
  }

  /**
   * Check if the wave is actively being processed.
   */
  isActive(): boolean {
    return (
      this.status === WaveStatus.RELEASED ||
      this.status === WaveStatus.IN_PROGRESS
    );
  }

  /**
   * Check if the wave has a workflow running.
   */
  hasActiveWorkflow(): boolean {
    return !!this.workflowId && !this.isTerminal();
  }

  /**
   * Get the order count.
   */
  getOrderCount(): number {
    return this.orderIds.length;
  }

  /**
   * Check if the wave is empty.
   */
  isEmpty(): boolean {
    return this.orderIds.length === 0;
  }

  /**
   * Get the workflow ID for display.
   */
  getWorkflowIdDisplay(): string {
    return this.workflowId ?? `wave-execution-${this.id}`;
  }
}

