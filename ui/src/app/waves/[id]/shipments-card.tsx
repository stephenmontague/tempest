"use client";

import { useState, useTransition, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Truck, Printer, CheckCircle2, DollarSign, Loader2, Package, ExternalLink } from "lucide-react";
import { getShipmentStates, signalPrintLabel, signalShipmentConfirmed, ShipmentState } from "@/app/actions/waves";
import { toast } from "sonner";

interface ShipmentsCardProps {
     waveId: number;
     currentStep?: string;
     onOpenRateModal: (shipmentId: number) => void;
}

export function ShipmentsCard({ waveId, currentStep, onOpenRateModal }: ShipmentsCardProps) {
     const router = useRouter();
     const [isPending, startTransition] = useTransition();
     const [shipments, setShipments] = useState<Record<number, ShipmentState>>({});
     const [loadingAction, setLoadingAction] = useState<string | null>(null);

     // Show shipments card only when we're in the shipping phase
     const showShipments =
          currentStep === "WAITING_FOR_SHIPMENTS" ||
          currentStep === "CREATING_SHIPMENTS" ||
          currentStep === "MARKING_SHIPPED" ||
          currentStep === "COMPLETED";

     // Poll for shipment states
     useEffect(() => {
          if (!showShipments) return;

          const fetchShipments = async () => {
               const result = await getShipmentStates(waveId);
               if (result.success && result.data) {
                    setShipments(result.data);
               }
          };

          fetchShipments();
          const interval = setInterval(fetchShipments, 2000);
          return () => clearInterval(interval);
     }, [waveId, showShipments]);

     const handlePrintLabel = (shipmentId: number) => {
          setLoadingAction(`print-${shipmentId}`);
          startTransition(async () => {
               const result = await signalPrintLabel(waveId, shipmentId);

               if (result.success) {
                    toast.success("Label generated", {
                         description: "The shipping label has been generated",
                    });
                    router.refresh();
               } else {
                    toast.error("Failed to print label", {
                         description: result.error,
                    });
               }
               setLoadingAction(null);
          });
     };

     const handleConfirmShipped = (shipmentId: number) => {
          setLoadingAction(`confirm-${shipmentId}`);
          startTransition(async () => {
               const result = await signalShipmentConfirmed(waveId, shipmentId);

               if (result.success) {
                    toast.success("Shipment confirmed", {
                         description: "The shipment has been marked as shipped",
                    });
                    router.refresh();
               } else {
                    toast.error("Failed to confirm shipment", {
                         description: result.error,
                    });
               }
               setLoadingAction(null);
          });
     };

     const getStatusBadge = (status: string) => {
          switch (status) {
               case "CREATED":
                    return <Badge variant="outline">Created</Badge>;
               case "RATE_SELECTED":
                    return <Badge variant="secondary">Rate Selected</Badge>;
               case "LABEL_GENERATED":
                    return <Badge className="bg-blue-500">Label Ready</Badge>;
               case "SHIPPED":
                    return <Badge className="bg-green-500">Shipped</Badge>;
               default:
                    return <Badge variant="outline">{status}</Badge>;
          }
     };

     if (!showShipments) {
          return null;
     }

     const shipmentList = Object.values(shipments);

     if (shipmentList.length === 0) {
          return (
               <Card>
                    <CardHeader>
                         <CardTitle className="text-base flex items-center gap-2">
                              <Truck className="h-5 w-5" />
                              Shipments
                         </CardTitle>
                    </CardHeader>
                    <CardContent>
                         <p className="text-sm text-muted-foreground text-center py-4">Creating shipments...</p>
                    </CardContent>
               </Card>
          );
     }

     return (
          <Card>
               <CardHeader>
                    <CardTitle className="text-base flex items-center gap-2">
                         <Truck className="h-5 w-5" />
                         Shipments ({shipmentList.length})
                    </CardTitle>
               </CardHeader>
               <CardContent className="space-y-4">
                    {shipmentList.map((shipment) => (
                         <div key={shipment.shipmentId} className="border rounded-lg p-4 space-y-3">
                              {/* Header */}
                              <div className="flex items-center justify-between">
                                   <div className="flex items-center gap-2">
                                        <Package className="h-4 w-4 text-muted-foreground" />
                                        <span className="font-mono text-sm">Order #{shipment.orderId}</span>
                                   </div>
                                   {getStatusBadge(shipment.status)}
                              </div>

                              {/* Carrier info */}
                              {shipment.carrier && shipment.carrier !== "PENDING" && (
                                   <div className="text-sm text-muted-foreground">
                                        {shipment.carrier} - {shipment.serviceLevel}
                                   </div>
                              )}

                              {/* Tracking number */}
                              {shipment.trackingNumber && (
                                   <div className="flex items-center gap-2 text-sm">
                                        <span className="text-muted-foreground">Tracking:</span>
                                        <span className="font-mono">{shipment.trackingNumber}</span>
                                        {shipment.labelUrl && (
                                             <a
                                                  href={shipment.labelUrl}
                                                  target="_blank"
                                                  rel="noopener noreferrer"
                                                  className="text-primary hover:underline">
                                                  <ExternalLink className="h-3 w-3" />
                                             </a>
                                        )}
                                   </div>
                              )}

                              {/* Actions based on status */}
                              <div className="flex flex-wrap gap-2 pt-2">
                                   {/* Get Rates - available when CREATED */}
                                   {(shipment.status === "CREATED" || shipment.status === "RATE_SELECTED") && (
                                        <Button
                                             variant="outline"
                                             size="sm"
                                             onClick={() => onOpenRateModal(shipment.shipmentId)}
                                             disabled={isPending}>
                                             <DollarSign className="h-3 w-3 mr-1" />
                                             Get Rates
                                        </Button>
                                   )}

                                   {/* Print Label - available when CREATED or RATE_SELECTED */}
                                   {(shipment.status === "CREATED" || shipment.status === "RATE_SELECTED") && (
                                        <Button
                                             variant="default"
                                             size="sm"
                                             onClick={() => handlePrintLabel(shipment.shipmentId)}
                                             disabled={isPending || loadingAction === `print-${shipment.shipmentId}`}>
                                             {loadingAction === `print-${shipment.shipmentId}` ? (
                                                  <Loader2 className="h-3 w-3 mr-1 animate-spin" />
                                             ) : (
                                                  <Printer className="h-3 w-3 mr-1" />
                                             )}
                                             Print Label
                                        </Button>
                                   )}

                                   {/* Confirm Shipped - available when LABEL_GENERATED */}
                                   {shipment.status === "LABEL_GENERATED" && (
                                        <Button
                                             variant="default"
                                             size="sm"
                                             onClick={() => handleConfirmShipped(shipment.shipmentId)}
                                             disabled={isPending || loadingAction === `confirm-${shipment.shipmentId}`}
                                             className="bg-green-600 hover:bg-green-700">
                                             {loadingAction === `confirm-${shipment.shipmentId}` ? (
                                                  <Loader2 className="h-3 w-3 mr-1 animate-spin" />
                                             ) : (
                                                  <CheckCircle2 className="h-3 w-3 mr-1" />
                                             )}
                                             Confirm Shipped
                                        </Button>
                                   )}

                                   {/* Shipped indicator */}
                                   {shipment.status === "SHIPPED" && (
                                        <div className="flex items-center gap-1 text-green-600 text-sm">
                                             <CheckCircle2 className="h-4 w-4" />
                                             Shipped
                                        </div>
                                   )}
                              </div>
                         </div>
                    ))}
               </CardContent>
          </Card>
     );
}
