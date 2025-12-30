"use server";

import { revalidatePath } from "next/cache";
import { getImsClient, CreateItemRequest } from "@/services/ims-client";

export interface ActionResult<T = void> {
  success: boolean;
  data?: T;
  error?: string;
}

/**
 * Create a new item.
 */
export async function createItem(
  request: CreateItemRequest
): Promise<ActionResult<{ itemId: number }>> {
  try {
    const client = getImsClient();

    const item = await client.createItem(request);

    revalidatePath("/items");
    revalidatePath(`/items/${item.id}`);

    return {
      success: true,
      data: {
        itemId: item.id,
      },
    };
  } catch (error) {
    console.error("Failed to create item:", error);
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to create item",
    };
  }
}

/**
 * Update an existing item.
 */
export async function updateItem(
  itemId: number,
  request: Partial<CreateItemRequest>
): Promise<ActionResult> {
  try {
    const client = getImsClient();

    await client.updateItem(itemId, request);

    revalidatePath("/items");
    revalidatePath(`/items/${itemId}`);

    return { success: true };
  } catch (error) {
    console.error("Failed to update item:", error);
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to update item",
    };
  }
}

/**
 * Delete an item (soft delete - set inactive).
 */
export async function deleteItem(itemId: number): Promise<ActionResult> {
  try {
    const client = getImsClient();

    await client.deleteItem(itemId);

    revalidatePath("/items");

    return { success: true };
  } catch (error) {
    console.error("Failed to delete item:", error);
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to delete item",
    };
  }
}
