import { NextRequest, NextResponse } from "next/server";
import { getWmsClient } from "@/services/wms-client";

interface RouteParams {
  params: Promise<{ id: string }>;
}

/**
 * GET /api/waves/[id]
 * Get a specific wave by ID.
 */
export async function GET(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getWmsClient();
    const { id } = await params;
    const waveId = parseInt(id, 10);

    if (isNaN(waveId)) {
      return NextResponse.json({ error: "Invalid wave ID" }, { status: 400 });
    }

    const wave = await client.getWave(waveId);

    return NextResponse.json(wave);
  } catch (error) {
    console.error("Failed to fetch wave:", error);
    return NextResponse.json(
      { error: "Failed to fetch wave" },
      { status: 500 }
    );
  }
}

/**
 * DELETE /api/waves/[id]
 * Cancel a wave.
 */
export async function DELETE(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getWmsClient();
    const { id } = await params;
    const waveId = parseInt(id, 10);

    if (isNaN(waveId)) {
      return NextResponse.json({ error: "Invalid wave ID" }, { status: 400 });
    }

    const searchParams = request.nextUrl.searchParams;
    const reason = searchParams.get("reason") ?? "Cancelled by user";

    const wave = await client.cancelWave(waveId, reason);

    return NextResponse.json(wave);
  } catch (error) {
    console.error("Failed to cancel wave:", error);
    return NextResponse.json(
      { error: "Failed to cancel wave" },
      { status: 500 }
    );
  }
}
