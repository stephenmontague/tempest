"use server";

import { revalidatePath } from "next/cache";
import { getOmsClient, CreateOrderRequest } from "@/services/oms-client";

export interface OrderIntakeRequest {
  externalOrderId: string;
  channel?: string;
  priority?: string;
  facilityId: number;
  customerEmail?: string;
  customerName?: string;
  shippingAddressLine1?: string;
  shippingAddressLine2?: string;
  shippingCity?: string;
  shippingState?: string;
  shippingPostalCode?: string;
  shippingCountry?: string;
  lines: {
    sku: string;
    quantity: number;
    unitPrice?: number;
  }[];
}

export interface ActionResult<T = void> {
  success: boolean;
  data?: T;
  error?: string;
}

/**
 * Create a new order via the OrderIntakeWorkflow.
 * This executes the Temporal workflow synchronously and returns the created order.
 */
export async function createOrder(
  request: OrderIntakeRequest
): Promise<ActionResult<{ orderId: number; status: string; externalOrderId: string }>> {
  try {
    const client = getOmsClient();

    // Call OMS to execute the order intake workflow (waits for completion)
    const orderRequest: CreateOrderRequest = {
      externalOrderId: request.externalOrderId,
      customerEmail: request.customerEmail,
      customerName: request.customerName,
      shippingAddressLine1: request.shippingAddressLine1,
      shippingAddressLine2: request.shippingAddressLine2,
      shippingCity: request.shippingCity,
      shippingState: request.shippingState,
      shippingPostalCode: request.shippingPostalCode,
      shippingCountry: request.shippingCountry,
      lines: request.lines,
    };

    const response = await client.createOrder(orderRequest);

    // Revalidate order pages
    revalidatePath("/orders");
    revalidatePath(`/orders/${response.orderId}`);

    return {
      success: true,
      data: {
        orderId: response.orderId,
        status: response.status,
        externalOrderId: response.externalOrderId,
      },
    };
  } catch (error) {
    console.error("Failed to create order:", error);
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to create order",
    };
  }
}

/**
 * Get the workflow status for an order.
 */
export async function getOrderWorkflowStatus(
  workflowId: string
): Promise<ActionResult<{ status: string; currentStep?: string; blockingReason?: string | null }>> {
  try {
    const client = getOmsClient();

    // Call OMS to get workflow status
    const status = await client.getOrderWorkflowStatus(workflowId);

    return {
      success: true,
      data: status,
    };
  } catch (error) {
    console.error("Failed to get workflow status:", error);
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to get workflow status",
    };
  }
}

/**
 * Cancel an order.
 */
export async function cancelOrder(
  orderId: number,
  reason: string
): Promise<ActionResult> {
  try {
    const client = getOmsClient();

    await client.cancelOrder(orderId, reason);

    revalidatePath("/orders");
    revalidatePath(`/orders/${orderId}`);

    return { success: true };
  } catch (error) {
    console.error("Failed to cancel order:", error);
    return {
      success: false,
      error: error instanceof Error ? error.message : "Failed to cancel order",
    };
  }
}
