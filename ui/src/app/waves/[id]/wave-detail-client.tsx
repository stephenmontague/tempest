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
     FulfillmentMode,
} from "@/app/actions/waves";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
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
     const [showReleaseDialog, setShowReleaseDialog] = useState(false);
     const [rateModalShipmentId, setRateModalShipmentId] = useState<number | null>(null);

     // Release dialog state
     const [fulfillmentMode, setFulfillmentMode] = useState<FulfillmentMode>("STANDARD");
     const [defaultCarrier, setDefaultCarrier] = useState("USPS");
     const [defaultServiceLevel, setDefaultServiceLevel] = useState("GROUND");

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
               // Build release request with order details and fulfillment mode
               const request: ReleaseWaveRequest = {
                    orders: orders.map((order) => ({
                         orderId: order.id,
                         externalOrderId: order.externalOrderId,
                         orderLines: [], // Would need to fetch from backend
                         shipTo: undefined,
                    })),
                    fulfillmentMode,
                    defaultCarrier: fulfillmentMode !== "STANDARD" ? defaultCarrier : undefined,
                    defaultServiceLevel: fulfillmentMode !== "STANDARD" ? defaultServiceLevel : undefined,
               };

               const result = await releaseWave(waveId, request);

               if (result.success) {
                    const modeDescription =
                         fulfillmentMode === "AUTO_SHIP"
                              ? "Auto-ship mode - orders will be shipped automatically"
                              : fulfillmentMode === "EXPRESS"
                              ? "Express mode - using default carrier"
                              : "Standard mode - full HITL";
                    toast.success("Wave released successfully", {
                         description: modeDescription,
                    });
                    setShowReleaseDialog(false);
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
                              <Dialog open={showReleaseDialog} onOpenChange={setShowReleaseDialog}>
                                   <DialogTrigger asChild>
                                        <Button className="w-full justify-start" disabled={isPending}>
                                             <Play className="h-4 w-4 mr-2" />
                                             Release Wave
                                        </Button>
                                   </DialogTrigger>
                                   <DialogContent className="sm:max-w-md">
                                        <DialogHeader>
                                             <DialogTitle>Release Wave</DialogTitle>
                                             <DialogDescription>
                                                  Choose how this wave should be fulfilled. This affects the level of
                                                  human interaction required.
                                             </DialogDescription>
                                        </DialogHeader>
                                        <div className="space-y-4 py-4">
                                             <div className="space-y-3">
                                                  <Label>Fulfillment Mode</Label>
                                                  <RadioGroup
                                                       value={fulfillmentMode}
                                                       onValueChange={(value) =>
                                                            setFulfillmentMode(value as FulfillmentMode)
                                                       }
                                                       className="space-y-2">
                                                       <div className="flex items-start space-x-3">
                                                            <RadioGroupItem
                                                                 value="STANDARD"
                                                                 id="standard"
                                                                 className="mt-1"
                                                            />
                                                            <div>
                                                                 <Label
                                                                      htmlFor="standard"
                                                                      className="font-medium cursor-pointer">
                                                                      Standard
                                                                 </Label>
                                                                 <p className="text-sm text-muted-foreground">
                                                                      Full HITL with rate shopping, manual label
                                                                      printing, and ship confirmation
                                                                 </p>
                                                            </div>
                                                       </div>
                                                       <div className="flex items-start space-x-3">
                                                            <RadioGroupItem
                                                                 value="EXPRESS"
                                                                 id="express"
                                                                 className="mt-1"
                                                            />
                                                            <div>
                                                                 <Label
                                                                      htmlFor="express"
                                                                      className="font-medium cursor-pointer">
                                                                      Express
                                                                 </Label>
                                                                 <p className="text-sm text-muted-foreground">
                                                                      Skip rate shopping, use default carrier. Manual
                                                                      label and ship confirmation.
                                                                 </p>
                                                            </div>
                                                       </div>
                                                       <div className="flex items-start space-x-3">
                                                            <RadioGroupItem
                                                                 value="AUTO_SHIP"
                                                                 id="auto-ship"
                                                                 className="mt-1"
                                                            />
                                                            <div>
                                                                 <Label
                                                                      htmlFor="auto-ship"
                                                                      className="font-medium cursor-pointer">
                                                                      Auto-Ship
                                                                 </Label>
                                                                 <p className="text-sm text-muted-foreground">
                                                                      Fully automated after packing. Labels generated
                                                                      and shipments confirmed automatically.
                                                                 </p>
                                                            </div>
                                                       </div>
                                                  </RadioGroup>
                                             </div>

                                             {fulfillmentMode !== "STANDARD" && (
                                                  <div className="space-y-3 pt-2 border-t">
                                                       <div className="space-y-2">
                                                            <Label htmlFor="carrier">Default Carrier</Label>
                                                            <Select
                                                                 value={defaultCarrier}
                                                                 onValueChange={setDefaultCarrier}>
                                                                 <SelectTrigger id="carrier">
                                                                      <SelectValue />
                                                                 </SelectTrigger>
                                                                 <SelectContent>
                                                                      <SelectItem value="USPS">USPS</SelectItem>
                                                                      <SelectItem value="UPS">UPS</SelectItem>
                                                                      <SelectItem value="FEDEX">FedEx</SelectItem>
                                                                 </SelectContent>
                                                            </Select>
                                                       </div>
                                                       <div className="space-y-2">
                                                            <Label htmlFor="serviceLevel">Service Level</Label>
                                                            <Select
                                                                 value={defaultServiceLevel}
                                                                 onValueChange={setDefaultServiceLevel}>
                                                                 <SelectTrigger id="serviceLevel">
                                                                      <SelectValue />
                                                                 </SelectTrigger>
                                                                 <SelectContent>
                                                                      <SelectItem value="GROUND">Ground</SelectItem>
                                                                      <SelectItem value="PRIORITY">Priority</SelectItem>
                                                                      <SelectItem value="EXPRESS">Express</SelectItem>
                                                                 </SelectContent>
                                                            </Select>
                                                       </div>
                                                  </div>
                                             )}
                                        </div>
                                        <DialogFooter>
                                             <Button variant="outline" onClick={() => setShowReleaseDialog(false)}>
                                                  Cancel
                                             </Button>
                                             <Button onClick={handleRelease} disabled={isPending}>
                                                  {isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                                                  Release Wave
                                             </Button>
                                        </DialogFooter>
                                   </DialogContent>
                              </Dialog>
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
