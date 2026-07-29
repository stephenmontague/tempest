import { NextRequest, NextResponse } from "next/server";

import { getTemporalClient } from "@/services/temporal-client";

interface RouteParams {
  params: Promise<{ workflowId: string }>;
}

/**
 * GET /api/demo/random-dag/[workflowId]/status
 * Query the status of a RandomDAGWorkflow directly via the Temporal client.
 */
export async function GET(_request: NextRequest, { params }: RouteParams) {
  try {
    const { workflowId } = await params;

    const client = await getTemporalClient();
    const handle = client.workflow.getHandle(workflowId);

    // Query names match the Java @QueryMethod method names.
    const [status, currentStep] = await Promise.all([
      handle.query<string>("getStatus"),
      handle.query<string>("getCurrentStep"),
    ]);

    return NextResponse.json({ status, currentStep });
  } catch (error) {
    console.error("Failed to get workflow status:", error);
    return NextResponse.json(
      { error: "Failed to get workflow status" },
      { status: 500 }
    );
  }
}
