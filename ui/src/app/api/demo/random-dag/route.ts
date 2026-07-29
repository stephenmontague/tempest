import { randomUUID } from "crypto";
import { NextRequest, NextResponse } from "next/server";

import { getTenantId } from "@/lib/auth/session";
import { getTemporalClient, WORKFLOW, temporalUiUrl } from "@/services/temporal-client";

/**
 * POST /api/demo/random-dag
 * Start a RandomDAGWorkflow with the specified step order.
 *
 * The UI is the Temporal client: this starts the (Java) workflow directly via the
 * Temporal TypeScript SDK, by workflow-type name on the OMS task queue.
 */
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const steps: string[] = Array.isArray(body?.steps) ? body.steps : [];

    const tenantId = await getTenantId();
    const requestId = randomUUID();
    const workflowId = WORKFLOW.randomDag.workflowId(requestId);

    const client = await getTemporalClient();
    await client.workflow.start(WORKFLOW.randomDag.type, {
      taskQueue: WORKFLOW.randomDag.taskQueue,
      workflowId,
      // Matches the Java RandomDAGWorkflowRequest { requestId, tenantId, steps }.
      args: [{ requestId, tenantId, steps }],
    });

    return NextResponse.json(
      { workflowId, temporalUiUrl: temporalUiUrl(workflowId) },
      { status: 201 }
    );
  } catch (error) {
    console.error("Failed to start RandomDAGWorkflow:", error);
    return NextResponse.json(
      { error: "Failed to start workflow" },
      { status: 500 }
    );
  }
}
