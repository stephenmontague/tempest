import { NextRequest, NextResponse } from "next/server";
import { getOmsClient } from "@/services/oms-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * GET /api/orders/[id]/workflow-status
 * Get the workflow status for an order.
 * Used for polling to update UI in real-time.
 */
export async function GET(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getOmsClient();
    const { id } = await params;

    // The workflow ID is typically "order-intake-{externalOrderId}" or passed as query param
    const searchParams = request.nextUrl.searchParams;
    const workflowId = searchParams.get("workflowId");

    if (!workflowId) {
      // Try to get the order first and use its workflow ID
      const orderId = parseInt(id, 10);
      if (isNaN(orderId)) {
        return NextResponse.json({ error: "Invalid order ID" }, { status: 400 });
      }

      const order = await client.getOrder(orderId);

      if (!order.workflowId) {
        return NextResponse.json({
          status: order.status,
          currentStep: null,
          blockingReason: null,
        });
      }

      const status = await client.getOrderWorkflowStatus(order.workflowId);
      return NextResponse.json(status);
    }

    const status = await client.getOrderWorkflowStatus(workflowId);
    return NextResponse.json(status);
  } catch (error) {
    console.error("Failed to fetch workflow status:", error);
    return NextResponse.json(
      { error: "Failed to fetch workflow status" },
      { status: 500 }
    );
  }
}
