import { NextRequest, NextResponse } from "next/server";
import { getImsClient } from "@/services/ims-client";
import { ServiceClientError } from "@/services/base-client";

interface RouteParams {
  params: Promise<{ sku: string }>;
}

/**
 * GET /api/items/sku/[sku] - Get an item by SKU
 * Proxies to IMS service.
 */
export async function GET(request: NextRequest, { params }: RouteParams) {
  try {
    const { sku } = await params;
    const imsClient = getImsClient();
    const item = await imsClient.getItemBySku(sku);

    if (!item) {
      return NextResponse.json(
        { error: "Not Found", message: `Item with SKU '${sku}' not found` },
        { status: 404 }
      );
    }

    return NextResponse.json(item);
  } catch (error) {
    if (error instanceof ServiceClientError) {
      return NextResponse.json(
        { error: error.error, message: error.message },
        { status: error.status }
      );
    }
    console.error("Error fetching item by SKU:", error);
    return NextResponse.json(
      { error: "Internal Server Error", message: "Failed to fetch item" },
      { status: 500 }
    );
  }
}

