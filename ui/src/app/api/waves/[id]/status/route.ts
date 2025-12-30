import { NextRequest, NextResponse } from "next/server";
import { getWmsClient } from "@/services/wms-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * GET /api/waves/[id]/status
 * Get the workflow status for a wave.
 * Used for polling to update UI in real-time.
 */
export async function GET(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getWmsClient();
    const { id } = await params;
    const waveId = parseInt(id, 10);

    if (isNaN(waveId)) {
      return NextResponse.json({ error: "Invalid wave ID" }, { status: 400 });
    }

    const status = await client.getWaveWorkflowStatus(waveId);

    return NextResponse.json(status);
  } catch (error) {
    console.error("Failed to fetch wave workflow status:", error);
    return NextResponse.json(
      { error: "Failed to fetch workflow status" },
      { status: 500 }
    );
  }
}
