import { NextRequest, NextResponse } from "next/server";
import { getOmsClient } from "@/services/oms-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * GET /api/orders/[id]/workflow-status
 * Returns the order's current lifecycle status.
 *
 * Order intake is a plain CRUD operation (not a Temporal workflow), so the
 * "status" is simply the order row's status. Fulfillment progress is tracked
 * separately via the WMS wave workflow.
 */
export async function GET(_request: NextRequest, { params }: RouteParams) {
  try {
    const client = getOmsClient();
    const { id } = await params;

    const orderId = parseInt(id, 10);
    if (isNaN(orderId)) {
      return NextResponse.json({ error: "Invalid order ID" }, { status: 400 });
    }

    const order = await client.getOrder(orderId);

    return NextResponse.json({
      status: order.status,
      currentStep: null,
      blockingReason: null,
    });
  } catch (error) {
    console.error("Failed to fetch order status:", error);
    return NextResponse.json(
      { error: "Failed to fetch order status" },
      { status: 500 }
    );
  }
}
