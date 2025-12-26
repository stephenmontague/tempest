import { NextResponse } from "next/server";
import { requireAuth, requireAuthWithRoles } from "@/lib/auth/proxy";
import { getImsClient } from "@/services/ims-client";
import { ServiceClientError } from "@/services/base-client";

/**
 * GET /api/items - Get all items
 * Proxies to IMS service with authentication.
 */
export async function GET() {
     const authResult = await requireAuth();
     if (!authResult.authenticated) {
          return authResult.response;
     }

     const { session } = authResult;

     try {
          const imsClient = getImsClient();
          const items = await imsClient.getItems(session.accessToken);
          return NextResponse.json(items);
     } catch (error) {
          if (error instanceof ServiceClientError) {
               return NextResponse.json({ error: error.error, message: error.message }, { status: error.status });
          }
          console.error("Error fetching items:", error);
          return NextResponse.json(
               { error: "Internal Server Error", message: "Failed to fetch items" },
               { status: 500 }
          );
     }
}

/**
 * POST /api/items - Create a new item
 * Proxies to IMS service with authentication.
 * Requires ADMIN or MANAGER role.
 */
export async function POST(request: Request) {
     const authResult = await requireAuthWithRoles("ADMIN", "MANAGER");
     if (!authResult.authenticated) {
          return authResult.response;
     }

     const { session } = authResult;

     try {
          const body = await request.json();
          const imsClient = getImsClient();
          const item = await imsClient.createItem(body, session.accessToken);
          return NextResponse.json(item, { status: 201 });
     } catch (error) {
          if (error instanceof ServiceClientError) {
               return NextResponse.json({ error: error.error, message: error.message }, { status: error.status });
          }
          console.error("Error creating item:", error);
          return NextResponse.json(
               { error: "Internal Server Error", message: "Failed to create item" },
               { status: 500 }
          );
     }
}
