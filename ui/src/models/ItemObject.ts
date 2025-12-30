import { Item } from "@/services/ims-client";

/**
 * Domain object for Item.
 * Encapsulates business logic and derived properties.
 */
export class ItemObject {
  readonly id: number;
  readonly tenantId: string;
  readonly sku: string;
  readonly name: string;
  readonly description?: string;
  readonly active: boolean;
  readonly createdByUserId?: string;
  readonly updatedByUserId?: string;
  readonly createdAt: Date;
  readonly updatedAt: Date;

  private constructor(data: Item) {
    this.id = data.id;
    this.tenantId = data.tenantId;
    this.sku = data.sku;
    this.name = data.name;
    this.description = data.description;
    this.active = data.active;
    this.createdByUserId = data.createdByUserId;
    this.updatedByUserId = data.updatedByUserId;
    this.createdAt = new Date(data.createdAt);
    this.updatedAt = new Date(data.updatedAt);
  }

  /**
   * Create an ItemObject from API response.
   */
  static fromJSON(data: Item): ItemObject {
    return new ItemObject(data);
  }

  /**
   * Check if the item is active.
   */
  isActive(): boolean {
    return this.active;
  }

  /**
   * Check if the item can be used in orders.
   */
  isOrderable(): boolean {
    return this.active;
  }

  /**
   * Check if the item can be edited.
   */
  canEdit(): boolean {
    return true; // Items can always be edited
  }

  /**
   * Check if the item can be deactivated.
   */
  canDeactivate(): boolean {
    return this.active;
  }

  /**
   * Check if the item can be activated.
   */
  canActivate(): boolean {
    return !this.active;
  }

  /**
   * Get the display label for the item.
   */
  getDisplayLabel(): string {
    return `${this.sku} - ${this.name}`;
  }

  /**
   * Get the status label.
   */
  getStatusLabel(): string {
    return this.active ? "Active" : "Inactive";
  }

  /**
   * Check if the item has a description.
   */
  hasDescription(): boolean {
    return !!this.description && this.description.trim().length > 0;
  }
}

