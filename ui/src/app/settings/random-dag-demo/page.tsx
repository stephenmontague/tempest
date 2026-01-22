"use client";

import { useState, useCallback } from "react";
import { MainLayout } from "@/components/layout";
import { PageHeader } from "@/components/shared";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import ReactFlow, {
  Node,
  Edge,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  Connection,
  addEdge,
  MarkerType,
  EdgeProps,
  getBezierPath,
  EdgeLabelRenderer,
  BaseEdge,
} from "reactflow";
import "reactflow/dist/style.css";
import { Play, ExternalLink, RotateCcw, X } from "lucide-react";
import { toast } from "sonner";

// Step definitions with icons and colors
const STEP_DEFINITIONS = [
  {
    id: "ALLOCATE",
    label: "Allocate",
    icon: "🔵",
    description: "Reserve inventory",
    color: "#3b82f6",
  },
  {
    id: "EMAIL",
    label: "Email",
    icon: "📧",
    description: "Send notification",
    color: "#8b5cf6",
  },
  {
    id: "PRINT_LABEL",
    label: "Print Label",
    icon: "🏷️",
    description: "Generate shipping label",
    color: "#f59e0b",
  },
  {
    id: "SHIP",
    label: "Ship",
    icon: "📦",
    description: "Confirm shipment",
    color: "#10b981",
  },
];

// Initial node positions in a 2x2 grid with generous spacing for visible edges
const initialNodes: Node[] = STEP_DEFINITIONS.map((step, index) => ({
  id: step.id,
  type: "default",
  position: {
    x: (index % 2) * 350 + 50,   // 350px horizontal spacing between columns
    y: Math.floor(index / 2) * 300 + 50,  // 300px vertical spacing between rows
  },
  data: {
    label: (
      <div className="flex flex-col items-center gap-1 p-2">
        <div className="text-2xl">{step.icon}</div>
        <div className="font-semibold">{step.label}</div>
        <div className="text-xs text-muted-foreground">{step.description}</div>
      </div>
    ),
  },
  style: {
    background: step.color,
    color: "white",
    border: "2px solid white",
    borderRadius: "8px",
    width: 180,
    fontSize: "14px",
  },
}));

// Initial edges (sequential order)
const initialEdges: Edge[] = STEP_DEFINITIONS.slice(0, -1).map((step, index) => ({
  id: `${step.id}-${STEP_DEFINITIONS[index + 1].id}`,
  source: step.id,
  target: STEP_DEFINITIONS[index + 1].id,
  type: "custom",
  animated: true,
  markerEnd: {
    type: MarkerType.ArrowClosed,
    color: "#64748b",
  },
  style: {
    stroke: "#64748b",
    strokeWidth: 2,
  },
}));

export default function RandomDAGDemoPage() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [workflowId, setWorkflowId] = useState<string | null>(null);
  const [temporalUiUrl, setTemporalUiUrl] = useState<string | null>(null);
  const [isExecuting, setIsExecuting] = useState(false);

  // Custom edge component with delete button
  const CustomEdge = useCallback(
    ({
      id,
      sourceX,
      sourceY,
      targetX,
      targetY,
      sourcePosition,
      targetPosition,
      style = {},
      markerEnd,
    }: EdgeProps) => {
      const [edgePath, labelX, labelY] = getBezierPath({
        sourceX,
        sourceY,
        sourcePosition,
        targetX,
        targetY,
        targetPosition,
      });

      const onEdgeClick = (evt: React.MouseEvent) => {
        evt.stopPropagation();
        setEdges((eds) => eds.filter((e) => e.id !== id));
        toast.info("Edge deleted");
      };

      return (
        <>
          <BaseEdge path={edgePath} markerEnd={markerEnd} style={style} />
          <EdgeLabelRenderer>
            <div
              style={{
                position: "absolute",
                transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
                fontSize: 12,
                pointerEvents: "all",
              }}
              className="nodrag nopan"
            >
              <button
                className="flex items-center justify-center w-6 h-6 bg-red-500 hover:bg-red-600 text-white rounded-full border-2 border-white shadow-lg transition-all hover:scale-110"
                onClick={onEdgeClick}
                title="Delete edge"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </EdgeLabelRenderer>
        </>
      );
    },
    [setEdges]
  );

  const edgeTypes = {
    custom: CustomEdge,
  };

  const onConnect = useCallback(
    (params: Connection) =>
      setEdges((eds) =>
        addEdge(
          {
            ...params,
            type: "custom",
            animated: true,
            markerEnd: {
              type: MarkerType.ArrowClosed,
              color: "#64748b",
            },
            style: {
              stroke: "#64748b",
              strokeWidth: 2,
            },
          },
          eds
        )
      ),
    [setEdges]
  );

  const resetDAG = () => {
    setNodes(initialNodes);
    setEdges(initialEdges);
    setWorkflowId(null);
    setTemporalUiUrl(null);
    toast.info("DAG reset to default order");
  };

  const extractStepOrder = (): string[] => {
    // Build adjacency list from edges
    const adjacency = new Map<string, string[]>();
    const inDegree = new Map<string, number>();

    // Initialize all nodes
    STEP_DEFINITIONS.forEach((step) => {
      adjacency.set(step.id, []);
      inDegree.set(step.id, 0);
    });

    // Build graph
    edges.forEach((edge) => {
      adjacency.get(edge.source)?.push(edge.target);
      inDegree.set(edge.target, (inDegree.get(edge.target) || 0) + 1);
    });

    // Topological sort (Kahn's algorithm)
    const queue: string[] = [];
    const result: string[] = [];

    // Find all nodes with no incoming edges
    inDegree.forEach((degree, node) => {
      if (degree === 0) {
        queue.push(node);
      }
    });

    while (queue.length > 0) {
      const node = queue.shift()!;
      result.push(node);

      adjacency.get(node)?.forEach((neighbor) => {
        const newDegree = (inDegree.get(neighbor) || 0) - 1;
        inDegree.set(neighbor, newDegree);
        if (newDegree === 0) {
          queue.push(neighbor);
        }
      });
    }

    // If result doesn't contain all nodes, there's a cycle or disconnected nodes
    if (result.length !== STEP_DEFINITIONS.length) {
      // Add any missing nodes at the end
      STEP_DEFINITIONS.forEach((step) => {
        if (!result.includes(step.id)) {
          result.push(step.id);
        }
      });
    }

    return result;
  };

  const executeWorkflow = async () => {
    setIsExecuting(true);

    try {
      const stepOrder = extractStepOrder();

      toast.info(`Executing workflow with order: ${stepOrder.join(" → ")}`);

      const response = await fetch("/api/demo/random-dag", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ steps: stepOrder }),
      });

      if (!response.ok) {
        throw new Error("Failed to start workflow");
      }

      const data = await response.json();

      setWorkflowId(data.workflowId);
      setTemporalUiUrl(data.temporalUiUrl);

      toast.success("Workflow started successfully!", {
        description: `Workflow ID: ${data.workflowId}`,
        action: {
          label: "View in Temporal",
          onClick: () => window.open(data.temporalUiUrl, "_blank"),
        },
      });
    } catch (error) {
      console.error("Failed to execute workflow:", error);
      toast.error("Failed to start workflow", {
        description: error instanceof Error ? error.message : "Unknown error",
      });
    } finally {
      setIsExecuting(false);
    }
  };

  return (
    <MainLayout>
      <PageHeader
        title="Random DAG Demo"
        description="Drag nodes to reorder, connect edges to define execution flow, then execute the workflow"
      />

      <div className="grid gap-6 lg:grid-cols-3">
        {/* DAG Editor */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>DAG Editor</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-[600px] border rounded-lg bg-muted/20">
              <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                edgeTypes={edgeTypes}
                fitView
              >
                <Controls />
                <Background />
              </ReactFlow>
            </div>
          </CardContent>
        </Card>

        {/* Control Panel */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Controls</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button
                onClick={executeWorkflow}
                disabled={isExecuting}
                className="w-full"
                size="lg"
              >
                <Play className="h-4 w-4 mr-2" />
                {isExecuting ? "Executing..." : "Execute Workflow"}
              </Button>

              <Button
                onClick={resetDAG}
                variant="outline"
                className="w-full"
              >
                <RotateCcw className="h-4 w-4 mr-2" />
                Reset to Default
              </Button>
            </CardContent>
          </Card>

          {workflowId && (
            <Card>
              <CardHeader>
                <CardTitle>Execution Info</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div>
                  <div className="text-sm text-muted-foreground mb-1">
                    Workflow ID
                  </div>
                  <div className="font-mono text-xs break-all bg-muted p-2 rounded">
                    {workflowId}
                  </div>
                </div>

                {temporalUiUrl && (
                  <Button
                    variant="outline"
                    className="w-full"
                    onClick={() => window.open(temporalUiUrl, "_blank")}
                  >
                    <ExternalLink className="h-4 w-4 mr-2" />
                    View in Temporal UI
                  </Button>
                )}

                <div className="pt-2 border-t">
                  <div className="text-sm text-muted-foreground mb-2">
                    Current Order
                  </div>
                  <div className="flex flex-col gap-1">
                    {extractStepOrder().map((stepId, index) => {
                      const step = STEP_DEFINITIONS.find((s) => s.id === stepId);
                      return (
                        <div
                          key={stepId}
                          className="flex items-center gap-2 text-sm"
                        >
                          <Badge variant="outline" className="w-6 h-6 p-0 justify-center">
                            {index + 1}
                          </Badge>
                          <span>{step?.icon}</span>
                          <span>{step?.label}</span>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader>
              <CardTitle>Instructions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-muted-foreground">
              <p>
                <strong>Drag nodes</strong> to reposition them on the canvas.
              </p>
              <p>
                <strong>Delete edges</strong> by clicking the red X button on the edge.
              </p>
              <p>
                <strong>Create edges</strong> by dragging from one node's handle to another.
              </p>
              <p>
                <strong>Execute</strong> to start a Temporal workflow with your custom order.
              </p>
              <p className="pt-2 border-t">
                The workflow will execute activities in the order defined by your DAG topology.
                Watch the execution in the Temporal UI to see deterministic processing!
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </MainLayout>
  );
}
