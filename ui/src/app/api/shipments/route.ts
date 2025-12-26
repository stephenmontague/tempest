import { NextResponse } from "next/server";
import { requireAuth, requireAuthWithRoles } from "@/lib/auth/proxy";
import { getSmsClient } from "@/services/sms-client";
import { ServiceClientError } from "@/services/base-client";

/**
 * GET /api/shipments - Get all shipments
 * Proxies to SMS service with authentication.
 */
export async function GET() {
     const authResult = await requireAuth();
     if (!authResult.authenticated) {
          return authResult.response;
     }

     const { session } = authResult;

     try {
          const smsClient = getSmsClient();
          const shipments = await smsClient.getShipments(session.accessToken);
          return NextResponse.json(shipments);
     } catch (error) {
          if (error instanceof ServiceClientError) {
               return NextResponse.json({ error: error.error, message: error.message }, { status: error.status });
          }
          console.error("Error fetching shipments:", error);
          return NextResponse.json(
               { error: "Internal Server Error", message: "Failed to fetch shipments" },
               { status: 500 }
          );
     }
}

/**
 * POST /api/shipments - Create a new shipment
 * Proxies to SMS service with authentication.
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
          const smsClient = getSmsClient();
          const shipment = await smsClient.createShipment(body, session.accessToken);
          return NextResponse.json(shipment, { status: 201 });
     } catch (error) {
          if (error instanceof ServiceClientError) {
               return NextResponse.json({ error: error.error, message: error.message }, { status: error.status });
          }
          console.error("Error creating shipment:", error);
          return NextResponse.json(
               { error: "Internal Server Error", message: "Failed to create shipment" },
               { status: 500 }
          );
     }
}
