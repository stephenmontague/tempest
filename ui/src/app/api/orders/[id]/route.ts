import { NextRequest, NextResponse } from "next/server";
import { getOmsClient } from "@/services/oms-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * GET /api/orders/[id]
 * Get a specific order by ID.
 */
export async function GET(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getOmsClient();
    const { id } = await params;
    const orderId = parseInt(id, 10);

    if (isNaN(orderId)) {
      return NextResponse.json({ error: "Invalid order ID" }, { status: 400 });
    }

    const order = await client.getOrder(orderId);

    return NextResponse.json(order);
  } catch (error) {
    console.error("Failed to fetch order:", error);
    return NextResponse.json(
      { error: "Failed to fetch order" },
      { status: 500 }
    );
  }
}

/**
 * DELETE /api/orders/[id]
 * Cancel an order.
 */
export async function DELETE(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getOmsClient();
    const { id } = await params;
    const orderId = parseInt(id, 10);

    if (isNaN(orderId)) {
      return NextResponse.json({ error: "Invalid order ID" }, { status: 400 });
    }

    const searchParams = request.nextUrl.searchParams;
    const reason = searchParams.get("reason") ?? "Cancelled by user";

    await client.cancelOrder(orderId, reason);

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error("Failed to cancel order:", error);
    return NextResponse.json(
      { error: "Failed to cancel order" },
      { status: 500 }
    );
  }
}
