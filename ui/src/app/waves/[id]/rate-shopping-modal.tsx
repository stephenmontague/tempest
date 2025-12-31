"use client";

import {
     signalRateSelected,
     signalFetchRates,
     getFetchedRates,
     CarrierRate,
     FetchedRatesState,
} from "@/app/actions/waves";
import { Button } from "@/components/ui/button";
import {
     Dialog,
     DialogContent,
     DialogDescription,
     DialogFooter,
     DialogHeader,
     DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Badge } from "@/components/ui/badge";
import { CheckCircle2, Clock, DollarSign, Loader2, RefreshCw, Truck, XCircle, AlertTriangle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState, useTransition, useEffect, useCallback } from "react";
import { toast } from "sonner";

interface RateShoppingModalProps {
     waveId: number;
     shipmentId: number | null;
     open: boolean;
     onOpenChange: (open: boolean) => void;
}

export function RateShoppingModal({ waveId, shipmentId, open, onOpenChange }: RateShoppingModalProps) {
     const router = useRouter();
     const [isPending, startTransition] = useTransition();
     const [selectedRate, setSelectedRate] = useState<string | null>(null);
     const [fetchState, setFetchState] = useState<FetchedRatesState | null>(null);
     const [isFetching, setIsFetching] = useState(false);
     const [hasStartedFetch, setHasStartedFetch] = useState(false);

     // Poll for rates when fetching
     const pollRates = useCallback(async () => {
          if (!shipmentId || !hasStartedFetch) return;

          const result = await getFetchedRates(waveId, shipmentId);
          if (result.success && result.data) {
               setFetchState(result.data);

               // Stop polling when completed
               if (result.data.status === "COMPLETED" || result.data.status === "FAILED") {
                    setIsFetching(false);
               }
          }
     }, [waveId, shipmentId, hasStartedFetch]);

     useEffect(() => {
          if (!isFetching || !hasStartedFetch) return;

          const interval = setInterval(pollRates, 1000);
          return () => clearInterval(interval);
     }, [isFetching, hasStartedFetch, pollRates]);

     // Reset state when modal opens/closes
     useEffect(() => {
          if (!open) {
               setFetchState(null);
               setSelectedRate(null);
               setHasStartedFetch(false);
               setIsFetching(false);
          }
     }, [open]);

     const handleFetchRates = () => {
          if (!shipmentId) return;

          startTransition(async () => {
               const result = await signalFetchRates(waveId, shipmentId);

               if (result.success) {
                    toast.success("Fetching rates", {
                         description: "Contacting carriers... FedEx may take a moment.",
                    });
                    setHasStartedFetch(true);
                    setIsFetching(true);
                    setFetchState({
                         shipmentId,
                         status: "FETCHING",
                         rates: [],
                         uspsStatus: "FETCHING",
                         upsStatus: "FETCHING",
                         fedexStatus: "FETCHING",
                    });
               } else {
                    toast.error("Failed to fetch rates", {
                         description: result.error,
                    });
               }
          });
     };

     const handleSelectRate = () => {
          if (!selectedRate || !shipmentId) return;

          const [carrier, serviceLevel] = selectedRate.split("|");

          startTransition(async () => {
               const result = await signalRateSelected(waveId, shipmentId, carrier, serviceLevel);

               if (result.success) {
                    toast.success("Rate selected", {
                         description: `${carrier} ${serviceLevel} selected for shipment`,
                    });
                    onOpenChange(false);
                    setSelectedRate(null);
                    router.refresh();
               } else {
                    toast.error("Failed to select rate", {
                         description: result.error,
                    });
               }
          });
     };

     const handleClose = () => {
          setSelectedRate(null);
          setFetchState(null);
          setHasStartedFetch(false);
          setIsFetching(false);
          onOpenChange(false);
     };

     const getCarrierStatusBadge = (status: string | undefined) => {
          switch (status) {
               case "COMPLETED":
                    return (
                         <Badge variant="outline" className="bg-green-500/10 text-green-600 border-green-500/20">
                              <CheckCircle2 className="h-3 w-3 mr-1" />
                              Done
                         </Badge>
                    );
               case "FETCHING":
                    return (
                         <Badge variant="outline" className="bg-blue-500/10 text-blue-600 border-blue-500/20">
                              <Loader2 className="h-3 w-3 mr-1 animate-spin" />
                              Fetching
                         </Badge>
                    );
               case "FAILED":
                    return (
                         <Badge variant="outline" className="bg-red-500/10 text-red-600 border-red-500/20">
                              <XCircle className="h-3 w-3 mr-1" />
                              Failed
                         </Badge>
                    );
               default:
                    return (
                         <Badge variant="outline" className="text-muted-foreground">
                              Pending
                         </Badge>
                    );
          }
     };

     const rates = fetchState?.rates ?? [];

     return (
          <Dialog open={open} onOpenChange={handleClose}>
               <DialogContent className="sm:max-w-[550px]">
                    <DialogHeader>
                         <DialogTitle className="flex items-center gap-2">
                              <DollarSign className="h-5 w-5" />
                              Rate Shopping
                         </DialogTitle>
                         <DialogDescription>
                              Fetch live rates from carriers. FedEx will retry 4 times before succeeding (demo).
                         </DialogDescription>
                    </DialogHeader>

                    {/* Fetch Rates Button - shown when not yet fetched */}
                    {!hasStartedFetch && (
                         <div className="py-6 flex flex-col items-center gap-4">
                              <div className="text-center text-muted-foreground">
                                   <p>Click below to fetch rates from all carriers.</p>
                                   <p className="text-sm mt-1">
                                        This will call USPS, UPS, and FedEx in parallel via Temporal activities.
                                   </p>
                              </div>
                              <Button onClick={handleFetchRates} disabled={isPending} size="lg" className="gap-2">
                                   {isPending ? (
                                        <Loader2 className="h-4 w-4 animate-spin" />
                                   ) : (
                                        <RefreshCw className="h-4 w-4" />
                                   )}
                                   Fetch Rates from Carriers
                              </Button>
                         </div>
                    )}

                    {/* Carrier Status - shown while fetching */}
                    {hasStartedFetch && fetchState && fetchState.status !== "COMPLETED" && (
                         <div className="py-4 space-y-3">
                              <div className="text-sm font-medium text-muted-foreground mb-2">Carrier Status:</div>
                              <div className="grid grid-cols-3 gap-3">
                                   <div className="border rounded-lg p-3 text-center">
                                        <div className="font-medium mb-2">USPS</div>
                                        {getCarrierStatusBadge(fetchState.uspsStatus)}
                                   </div>
                                   <div className="border rounded-lg p-3 text-center">
                                        <div className="font-medium mb-2">UPS</div>
                                        {getCarrierStatusBadge(fetchState.upsStatus)}
                                   </div>
                                   <div className="border rounded-lg p-3 text-center">
                                        <div className="font-medium mb-2">FedEx</div>
                                        {getCarrierStatusBadge(fetchState.fedexStatus)}
                                        {fetchState.fedexStatus === "FETCHING" && (
                                             <div className="text-xs text-amber-600 mt-1 flex items-center justify-center gap-1">
                                                  <AlertTriangle className="h-3 w-3" />
                                                  Retrying...
                                             </div>
                                        )}
                                   </div>
                              </div>
                              <div className="text-center text-sm text-muted-foreground">
                                   <Loader2 className="h-4 w-4 inline mr-2 animate-spin" />
                                   Waiting for all carriers to respond...
                              </div>
                         </div>
                    )}

                    {/* Rates List - shown when completed */}
                    {hasStartedFetch && fetchState?.status === "COMPLETED" && rates.length > 0 && (
                         <div className="py-4">
                              <div className="flex items-center gap-2 mb-3 text-sm text-green-600">
                                   <CheckCircle2 className="h-4 w-4" />
                                   All rates fetched successfully!
                              </div>
                              <RadioGroup
                                   value={selectedRate ?? ""}
                                   onValueChange={setSelectedRate}
                                   className="space-y-3">
                                   {rates.map((rate: CarrierRate) => {
                                        const rateKey = `${rate.carrier}|${rate.serviceLevel}`;
                                        return (
                                             <div
                                                  key={rateKey}
                                                  className={`flex items-center space-x-3 border rounded-lg p-4 cursor-pointer transition-colors ${
                                                       selectedRate === rateKey
                                                            ? "border-primary bg-primary/5"
                                                            : "hover:bg-muted/50"
                                                  }`}
                                                  onClick={() => setSelectedRate(rateKey)}>
                                                  <RadioGroupItem value={rateKey} id={rateKey} />
                                                  <Label
                                                       htmlFor={rateKey}
                                                       className="flex-1 cursor-pointer flex items-center justify-between">
                                                       <div className="space-y-1">
                                                            <div className="flex items-center gap-2">
                                                                 <Truck className="h-4 w-4 text-muted-foreground" />
                                                                 <span className="font-medium">
                                                                      {rate.carrier} {rate.serviceLevel}
                                                                 </span>
                                                            </div>
                                                            <div className="flex items-center gap-1 text-sm text-muted-foreground">
                                                                 <Clock className="h-3 w-3" />
                                                                 {rate.estimatedDelivery}
                                                            </div>
                                                       </div>
                                                       <div className="text-lg font-semibold">${rate.price.toFixed(2)}</div>
                                                  </Label>
                                             </div>
                                        );
                                   })}
                              </RadioGroup>
                         </div>
                    )}

                    <DialogFooter>
                         <Button variant="outline" onClick={handleClose}>
                              Cancel
                         </Button>
                         {fetchState?.status === "COMPLETED" && (
                              <Button onClick={handleSelectRate} disabled={!selectedRate || isPending}>
                                   {isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                                   Select Rate
                              </Button>
                         )}
                    </DialogFooter>
               </DialogContent>
          </Dialog>
     );
}
