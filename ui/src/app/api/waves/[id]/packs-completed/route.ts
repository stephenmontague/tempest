import { NextRequest, NextResponse } from "next/server";
import { getWmsClient } from "@/services/wms-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * POST /api/waves/[id]/packs-completed
 * Signal that all packs in a wave are completed.
 */
export async function POST(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getWmsClient();
    const { id } = await params;
    const waveId = parseInt(id, 10);

    if (isNaN(waveId)) {
      return NextResponse.json({ error: "Invalid wave ID" }, { status: 400 });
    }

    await client.signalPacksCompleted(waveId);

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error("Failed to signal packs completed:", error);
    return NextResponse.json(
      { error: "Failed to signal packs completed" },
      { status: 500 }
    );
  }
}
