"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { useWorkflowPolling } from "@/hooks/use-polling";
import { WorkflowStatusCard } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
     Dialog,
     DialogContent,
     DialogDescription,
     DialogFooter,
     DialogHeader,
     DialogTitle,
     DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Play, CheckCircle2, Package, XCircle, Loader2, AlertTriangle } from "lucide-react";
import {
     releaseWave,
     signalPicksComplete,
     signalPacksComplete,
     cancelWave,
     getWaveWorkflowStatus,
     ReleaseWaveRequest,
} from "@/app/actions/waves";
import { toast } from "sonner";
import { ShipmentsCard } from "./shipments-card";
import { RateShoppingModal } from "./rate-shopping-modal";

interface Order {
     id: number;
     externalOrderId: string;
     status: string;
     customerName?: string;
}

interface WaveDetailClientProps {
     waveId: number;
     workflowId?: string;
     initialStatus: string;
     canRelease: boolean;
     canSignalPicks: boolean;
     canSignalPacks: boolean;
     canCancel: boolean;
     orders: Order[];
}

export function WaveDetailClient({
     waveId,
     workflowId,
     initialStatus,
     canRelease,
     canSignalPicks,
     canSignalPacks,
     canCancel,
     orders,
}: WaveDetailClientProps) {
     const router = useRouter();
     const [isPending, startTransition] = useTransition();
     const [cancelReason, setCancelReason] = useState("");
     const [showCancelDialog, setShowCancelDialog] = useState(false);
     const [rateModalShipmentId, setRateModalShipmentId] = useState<number | null>(null);

     const terminalStates = ["COMPLETED", "CANCELLED", "FAILED"];
     const isTerminal = terminalStates.includes(initialStatus);

     const { data, isPolling } = useWorkflowPolling(
          async () => {
               const result = await getWaveWorkflowStatus(waveId);
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

     // Dynamically determine which actions are available based on workflow step
     const showPicksButton =
          canSignalPicks &&
          (currentStep === "WAITING_FOR_PICKS" || currentStep === "CREATING_PICK_TASKS" || !currentStep);
     const showPacksButton =
          canSignalPacks &&
          (currentStep === "WAITING_FOR_PACKS" ||
               currentStep === "CONSUMING_INVENTORY" ||
               (currentStep && currentStep.includes("PACK")));

     const handleRelease = () => {
          startTransition(async () => {
               // Build release request with order details
               const request: ReleaseWaveRequest = {
                    orders: orders.map((order) => ({
                         orderId: order.id,
                         externalOrderId: order.externalOrderId,
                         orderLines: [], // Would need to fetch from backend
                         shipTo: undefined,
                    })),
               };

               const result = await releaseWave(waveId, request);

               if (result.success) {
                    toast.success("Wave released successfully", {
                         description: "Workflow started - monitoring progress",
                    });
                    router.refresh();
               } else {
                    toast.error("Failed to release wave", {
                         description: result.error,
                    });
               }
          });
     };

     const handleSignalPicks = () => {
          startTransition(async () => {
               const result = await signalPicksComplete(waveId);

               if (result.success) {
                    toast.success("Picks completed signal sent");
                    router.refresh();
               } else {
                    toast.error("Failed to signal picks complete", {
                         description: result.error,
                    });
               }
          });
     };

     const handleSignalPacks = () => {
          startTransition(async () => {
               const result = await signalPacksComplete(waveId);

               if (result.success) {
                    toast.success("Packs completed signal sent");
                    router.refresh();
               } else {
                    toast.error("Failed to signal packs complete", {
                         description: result.error,
                    });
               }
          });
     };

     const handleCancel = () => {
          startTransition(async () => {
               const result = await cancelWave(waveId, cancelReason || "Cancelled by user");

               if (result.success) {
                    toast.success("Wave cancelled");
                    setShowCancelDialog(false);
                    router.refresh();
               } else {
                    toast.error("Failed to cancel wave", {
                         description: result.error,
                    });
               }
          });
     };

     return (
          <>
               {/* Workflow Status */}
               <WorkflowStatusCard
                    workflowId={workflowId}
                    status={status}
                    currentStep={currentStep}
                    blockingReason={blockingReason}
                    isPolling={isPolling && !terminalStates.includes(status)}
               />

               {/* Actions */}
               <Card>
                    <CardHeader>
                         <CardTitle className="text-base">Actions</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                         {/* Release Wave */}
                         {canRelease && (
                              <Button className="w-full justify-start" onClick={handleRelease} disabled={isPending}>
                                   {isPending ? (
                                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                   ) : (
                                        <Play className="h-4 w-4 mr-2" />
                                   )}
                                   Release Wave
                              </Button>
                         )}

                         {/* Signal Picks Complete */}
                         {showPicksButton && (
                              <Button
                                   variant="outline"
                                   className="w-full justify-start"
                                   onClick={handleSignalPicks}
                                   disabled={isPending}>
                                   {isPending ? (
                                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                   ) : (
                                        <CheckCircle2 className="h-4 w-4 mr-2" />
                                   )}
                                   Signal Picks Complete
                              </Button>
                         )}

                         {/* Signal Packs Complete */}
                         {showPacksButton && (
                              <Button
                                   variant="outline"
                                   className="w-full justify-start"
                                   onClick={handleSignalPacks}
                                   disabled={isPending}>
                                   {isPending ? (
                                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                   ) : (
                                        <Package className="h-4 w-4 mr-2" />
                                   )}
                                   Signal Packs Complete
                              </Button>
                         )}

                         {/* Cancel Wave */}
                         {canCancel && (
                              <Dialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
                                   <DialogTrigger asChild>
                                        <Button
                                             variant="destructive"
                                             className="w-full justify-start"
                                             disabled={isPending}>
                                             <XCircle className="h-4 w-4 mr-2" />
                                             Cancel Wave
                                        </Button>
                                   </DialogTrigger>
                                   <DialogContent>
                                        <DialogHeader>
                                             <DialogTitle className="flex items-center gap-2">
                                                  <AlertTriangle className="h-5 w-5 text-destructive" />
                                                  Cancel Wave
                                             </DialogTitle>
                                             <DialogDescription>
                                                  This will cancel the wave and release any reserved inventory. This
                                                  action cannot be undone.
                                             </DialogDescription>
                                        </DialogHeader>
                                        <div className="space-y-2 py-4">
                                             <Label htmlFor="cancelReason">Reason (optional)</Label>
                                             <Input
                                                  id="cancelReason"
                                                  value={cancelReason}
                                                  onChange={(e) => setCancelReason(e.target.value)}
                                                  placeholder="Enter cancellation reason..."
                                             />
                                        </div>
                                        <DialogFooter>
                                             <Button variant="outline" onClick={() => setShowCancelDialog(false)}>
                                                  Keep Wave
                                             </Button>
                                             <Button variant="destructive" onClick={handleCancel} disabled={isPending}>
                                                  {isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                                                  Cancel Wave
                                             </Button>
                                        </DialogFooter>
                                   </DialogContent>
                              </Dialog>
                         )}

                         {isTerminal && (
                              <p className="text-sm text-muted-foreground text-center py-2">
                                   No actions available for {status.toLowerCase()} waves
                              </p>
                         )}
                    </CardContent>
               </Card>

               {/* Shipments Card - shown during shipping phase */}
               <ShipmentsCard
                    waveId={waveId}
                    currentStep={currentStep}
                    onOpenRateModal={(shipmentId) => setRateModalShipmentId(shipmentId)}
               />

               {/* Rate Shopping Modal */}
               <RateShoppingModal
                    waveId={waveId}
                    shipmentId={rateModalShipmentId}
                    open={rateModalShipmentId !== null}
                    onOpenChange={(open) => {
                         if (!open) setRateModalShipmentId(null);
                    }}
               />
          </>
     );
}
