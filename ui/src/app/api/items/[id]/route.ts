import { NextResponse } from "next/server";
import { requireAuth } from "@/lib/auth/proxy";
import { getImsClient } from "@/services/ims-client";
import { ServiceClientError } from "@/services/base-client";

interface RouteParams {
     params: Promise<{ id: string }>;
}

/**
 * GET /api/items/[id] - Get a specific item
 * Proxies to IMS service with authentication.
 */
export async function GET(request: Request, { params }: RouteParams) {
     const authResult = await requireAuth();
     if (!authResult.authenticated) {
          return authResult.response;
     }

     const { session } = authResult;
     const { id } = await params;
     const itemId = parseInt(id, 10);

     if (isNaN(itemId)) {
          return NextResponse.json({ error: "Bad Request", message: "Invalid item ID" }, { status: 400 });
     }

     try {
          const imsClient = getImsClient();
          const item = await imsClient.getItem(itemId, session.accessToken);
          return NextResponse.json(item);
     } catch (error) {
          if (error instanceof ServiceClientError) {
               return NextResponse.json({ error: error.error, message: error.message }, { status: error.status });
          }
          console.error("Error fetching item:", error);
          return NextResponse.json(
               { error: "Internal Server Error", message: "Failed to fetch item" },
               { status: 500 }
          );
     }
}
