"use client";

import { useWorkflowPolling } from "@/hooks/use-polling";
import { WorkflowStatusCard } from "@/components/shared";
import { getOrderWorkflowStatus } from "@/app/actions/orders";

interface OrderDetailClientProps {
  orderId: number;
  workflowId?: string;
  initialStatus: string;
}

export function OrderDetailClient({
  orderId,
  workflowId,
  initialStatus,
}: OrderDetailClientProps) {
  const terminalStates = ["SHIPPED", "DELIVERED", "CANCELLED", "FAILED"];
  const isTerminal = terminalStates.includes(initialStatus);

  const { data, isPolling } = useWorkflowPolling(
    async () => {
      if (!workflowId) {
        return { status: initialStatus };
      }
      const result = await getOrderWorkflowStatus(workflowId);
      if (result.success && result.data) {
        return result.data;
      }
      return { status: initialStatus };
    },
    {
      enabled: !!workflowId && !isTerminal,
      intervalMs: 2000,
    }
  );

  const status = data?.status ?? initialStatus;
  const currentStep = data?.currentStep;
  const blockingReason = data?.blockingReason;

  return (
    <WorkflowStatusCard
      workflowId={workflowId}
      status={status}
      currentStep={currentStep}
      blockingReason={blockingReason}
      isPolling={isPolling && !terminalStates.includes(status)}
    />
  );
}

