import { ServiceClientError } from "@/services/base-client";
import { getImsClient } from "@/services/ims-client";
import { NextRequest, NextResponse } from "next/server";

/**
 * GET /api/items/search - Search items by SKU prefix
 * Proxies to IMS service for autocomplete functionality.
 */
export async function GET(request: NextRequest) {
     try {
          const searchParams = request.nextUrl.searchParams;
          const query = searchParams.get("q") || "";

          const imsClient = getImsClient();
          const items = await imsClient.searchItems(query);
          return NextResponse.json(items);
     } catch (error) {
          if (error instanceof ServiceClientError) {
               return NextResponse.json({ error: error.error, message: error.message }, { status: error.status });
          }
          console.error("Error searching items:", error);
          return NextResponse.json(
               { error: "Internal Server Error", message: "Failed to search items" },
               { status: 500 }
          );
     }
}
