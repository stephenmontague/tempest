"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { StatusBadge } from "./status-badge";
import { cn } from "@/lib/utils";
import { Activity, Clock, AlertCircle, CheckCircle2 } from "lucide-react";
import { LoadingSpinner } from "./loading-spinner";

interface WorkflowStatusCardProps {
  workflowId?: string;
  status: string;
  currentStep?: string;
  blockingReason?: string | null;
  isPolling?: boolean;
  className?: string;
}

export function WorkflowStatusCard({
  workflowId,
  status,
  currentStep,
  blockingReason,
  isPolling,
  className,
}: WorkflowStatusCardProps) {
  const isTerminal = ["COMPLETED", "CANCELLED", "FAILED"].includes(status);
  const isWaiting = status.startsWith("WAITING");

  return (
    <Card className={cn("", className)}>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm font-medium flex items-center gap-2">
            <Activity className="h-4 w-4" />
            Workflow Status
            {isPolling && !isTerminal && (
              <LoadingSpinner size="sm" className="ml-1" />
            )}
          </CardTitle>
          <StatusBadge status={status} />
        </div>
      </CardHeader>
      <CardContent className="space-y-3">
        {workflowId && (
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Workflow ID</span>
            <code className="font-mono text-xs bg-muted px-2 py-1 rounded">
              {workflowId}
            </code>
          </div>
        )}

        {currentStep && (
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground flex items-center gap-1.5">
              <Clock className="h-3.5 w-3.5" />
              Current Step
            </span>
            <span className="font-medium">{currentStep.replace(/_/g, " ")}</span>
          </div>
        )}

        {blockingReason && (
          <div className="flex items-start gap-2 text-sm p-2 rounded-md bg-warning/10 border border-warning/20">
            <AlertCircle className="h-4 w-4 text-warning-foreground mt-0.5 shrink-0" />
            <div>
              <span className="font-medium text-warning-foreground">Waiting:</span>
              <span className="ml-1 text-muted-foreground">{blockingReason}</span>
            </div>
          </div>
        )}

        {isTerminal && status === "COMPLETED" && (
          <div className="flex items-center gap-2 text-sm p-2 rounded-md bg-success/10 border border-success/20">
            <CheckCircle2 className="h-4 w-4 text-success" />
            <span className="text-success">Workflow completed successfully</span>
          </div>
        )}

        {isTerminal && status === "FAILED" && (
          <div className="flex items-center gap-2 text-sm p-2 rounded-md bg-destructive/10 border border-destructive/20">
            <AlertCircle className="h-4 w-4 text-destructive" />
            <span className="text-destructive">Workflow failed</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

