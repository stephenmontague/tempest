import { NextRequest, NextResponse } from "next/server";
import { getWmsClient } from "@/services/wms-client";

/**
 * GET /api/waves
 * List all waves.
 */
export async function GET(request: NextRequest) {
  try {
    const client = getWmsClient();

    const searchParams = request.nextUrl.searchParams;
    const status = searchParams.get("status");
    const facilityId = searchParams.get("facilityId");

    let waves;
    if (status) {
      waves = await client.getWavesByStatus(status);
    } else if (facilityId) {
      waves = await client.getWavesByFacility(parseInt(facilityId, 10));
    } else {
      waves = await client.getWaves();
    }

    return NextResponse.json(waves);
  } catch (error) {
    console.error("Failed to fetch waves:", error);
    return NextResponse.json(
      { error: "Failed to fetch waves" },
      { status: 500 }
    );
  }
}

/**
 * POST /api/waves
 * Create a new wave.
 */
export async function POST(request: NextRequest) {
  try {
    const client = getWmsClient();
    const body = await request.json();

    const wave = await client.createWave(body);

    return NextResponse.json(wave, { status: 201 });
  } catch (error) {
    console.error("Failed to create wave:", error);
    return NextResponse.json(
      { error: "Failed to create wave" },
      { status: 500 }
    );
  }
}
