import { NextRequest, NextResponse } from "next/server";
import { getOmsClient } from "@/services/oms-client";

/**
 * POST /api/demo/random-dag
 * Start a RandomDAGWorkflow with the specified step order.
 */
export async function POST(request: NextRequest) {
  try {
    const client = getOmsClient();
    const body = await request.json();

    const result = await client.startRandomDAGWorkflow(body);

    return NextResponse.json(result, { status: 201 });
  } catch (error) {
    console.error("Failed to start RandomDAGWorkflow:", error);
    return NextResponse.json(
      { error: "Failed to start workflow" },
      { status: 500 }
    );
  }
}
