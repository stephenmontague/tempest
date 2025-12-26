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
   * Get all items for the authenticated tenant.
   */
  async getItems(accessToken: string): Promise<Item[]> {
    return this.get<Item[]>("/items", accessToken);
  }

  /**
   * Get a specific item by ID.
   */
  async getItem(id: number, accessToken: string): Promise<Item> {
    return this.get<Item>(`/items/${id}`, accessToken);
  }

  /**
   * Create a new item.
   */
  async createItem(
    request: CreateItemRequest,
    accessToken: string
  ): Promise<Item> {
    return this.post<Item, CreateItemRequest>(
      "/items",
      accessToken,
      request
    );
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

