import { BaseServiceClient } from "./base-client";

/**
 * Item entity from IMS.
 */
export interface Item {
  id: number;
  tenantId: string;
  sku: string;
  name: string;
  description?: string;
  active: boolean;
  createdByUserId?: string;
  updatedByUserId?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request to create a new item.
 */
export interface CreateItemRequest {
  sku: string;
  name: string;
  description?: string;
}

/**
 * IMS (Inventory Management System) service client.
 * Used server-side only - never expose to browser.
 */
export class ImsClient extends BaseServiceClient {
  constructor() {
    const baseUrl = process.env.IMS_URL;
    if (!baseUrl) {
      throw new Error("IMS_URL environment variable is not set");
    }
    super(baseUrl);
  }

  /**
   * Get all items.
   */
  async getItems(): Promise<Item[]> {
    return this.get<Item[]>("/items");
  }

  /**
   * Get active items only.
   */
  async getActiveItems(): Promise<Item[]> {
    return this.get<Item[]>("/items?active=true");
  }

  /**
   * Get a specific item by ID.
   */
  async getItem(id: number): Promise<Item> {
    return this.get<Item>(`/items/${id}`);
  }

  /**
   * Get an item by SKU.
   */
  async getItemBySku(sku: string): Promise<Item | null> {
    try {
      return await this.get<Item>(`/items/sku/${encodeURIComponent(sku)}`);
    } catch {
      return null;
    }
  }

  /**
   * Create a new item.
   */
  async createItem(request: CreateItemRequest): Promise<Item> {
    return this.post<Item, CreateItemRequest>("/items", request);
  }

  /**
   * Update an existing item.
   */
  async updateItem(id: number, request: Partial<CreateItemRequest>): Promise<Item> {
    return this.put<Item, Partial<CreateItemRequest>>(`/items/${id}`, request);
  }

  /**
   * Delete an item (soft delete - set inactive).
   */
  async deleteItem(id: number): Promise<void> {
    return this.delete<void>(`/items/${id}`);
  }

  /**
   * Get item count for dashboard.
   */
  async getItemCount(): Promise<{ total: number; active: number }> {
    return this.get<{ total: number; active: number }>("/items/count");
  }
}

// Singleton instance
let imsClient: ImsClient | null = null;

/**
 * Get the IMS client instance.
 * Creates a new instance if one doesn't exist.
 */
export function getImsClient(): ImsClient {
  if (!imsClient) {
    imsClient = new ImsClient();
  }
  return imsClient;
}
