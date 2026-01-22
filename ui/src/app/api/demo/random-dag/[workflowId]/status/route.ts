import { NextRequest, NextResponse } from "next/server";
import { getOmsClient } from "@/services/oms-client";

interface RouteParams {
  params: Promise<{ workflowId: string }>;
}

/**
 * GET /api/demo/random-dag/[workflowId]/status
 * Get the status of a RandomDAGWorkflow.
 */
export async function GET(request: NextRequest, { params }: RouteParams) {
  try {
    const client = getOmsClient();
    const { workflowId } = await params;

    const status = await client.getRandomDAGWorkflowStatus(workflowId);

    return NextResponse.json(status);
  } catch (error) {
    console.error("Failed to get workflow status:", error);
    return NextResponse.json(
      { error: "Failed to get workflow status" },
      { status: 500 }
    );
  }
}
