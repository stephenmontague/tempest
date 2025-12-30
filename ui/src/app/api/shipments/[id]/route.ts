import { NextResponse } from "next/server";
import { getSmsClient } from "@/services/sms-client";
import { ServiceClientError } from "@/services/base-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * GET /api/shipments/[id] - Get a specific shipment
 * Proxies to SMS service.
 */
export async function GET(request: Request, { params }: RouteParams) {
  const { id } = await params;
  const shipmentId = parseInt(id, 10);

  if (isNaN(shipmentId)) {
    return NextResponse.json(
      { error: "Bad Request", message: "Invalid shipment ID" },
      { status: 400 }
    );
  }

  try {
    const smsClient = getSmsClient();
    const shipment = await smsClient.getShipment(shipmentId);
    return NextResponse.json(shipment);
  } catch (error) {
    if (error instanceof ServiceClientError) {
      return NextResponse.json(
        { error: error.error, message: error.message },
        { status: error.status }
      );
    }
    console.error("Error fetching shipment:", error);
    return NextResponse.json(
      { error: "Internal Server Error", message: "Failed to fetch shipment" },
      { status: 500 }
    );
  }
}
