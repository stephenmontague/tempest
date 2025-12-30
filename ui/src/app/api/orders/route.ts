import { NextRequest, NextResponse } from "next/server";
import { getOmsClient } from "@/services/oms-client";

/**
 * GET /api/orders
 * List all orders. Supports filtering by status or sku query params.
 */
export async function GET(request: NextRequest) {
  try {
    const client = getOmsClient();

    const searchParams = request.nextUrl.searchParams;
    const status = searchParams.get("status");
    const sku = searchParams.get("sku");

    let orders;
    if (sku) {
      orders = await client.getOrdersBySku(sku);
    } else if (status) {
      orders = await client.getOrdersByStatus(status);
    } else {
      orders = await client.getOrders();
    }

    return NextResponse.json(orders);
  } catch (error) {
    console.error("Failed to fetch orders:", error);
    return NextResponse.json(
      { error: "Failed to fetch orders" },
      { status: 500 }
    );
  }
}

/**
 * POST /api/orders
 * Create a new order (triggers OrderIntakeWorkflow).
 */
export async function POST(request: NextRequest) {
  try {
    const client = getOmsClient();
    const body = await request.json();

    const order = await client.createOrder(body);

    return NextResponse.json(order, { status: 201 });
  } catch (error) {
    console.error("Failed to create order:", error);
    return NextResponse.json(
      { error: "Failed to create order" },
      { status: 500 }
    );
  }
}
