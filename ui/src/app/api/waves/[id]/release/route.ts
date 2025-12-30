import { NextRequest, NextResponse } from "next/server";
import { getWmsClient } from "@/services/wms-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * POST /api/waves/[id]/release
 * Release a wave for execution (starts WaveExecutionWorkflow).
 */
export async function POST(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getWmsClient();
    const { id } = await params;
    const waveId = parseInt(id, 10);

    if (isNaN(waveId)) {
      return NextResponse.json({ error: "Invalid wave ID" }, { status: 400 });
    }

    const body = await request.json();
    const wave = await client.releaseWave(waveId, body);

    return NextResponse.json(wave);
  } catch (error) {
    console.error("Failed to release wave:", error);
    return NextResponse.json(
      { error: "Failed to release wave" },
      { status: 500 }
    );
  }
}
