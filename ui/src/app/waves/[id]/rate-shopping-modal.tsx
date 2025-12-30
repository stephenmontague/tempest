"use client";

import { signalRateSelected } from "@/app/actions/waves";
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
import { Clock, DollarSign, Loader2, Truck } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";
import { toast } from "sonner";

interface CarrierRate {
     carrier: string;
     serviceLevel: string;
     price: number;
     estimatedDelivery: string;
}

// Fake rates - in production these would come from an API call
const FAKE_RATES: CarrierRate[] = [
     {
          carrier: "USPS",
          serviceLevel: "Priority",
          price: 6.25,
          estimatedDelivery: "2-3 business days",
     },
     {
          carrier: "UPS",
          serviceLevel: "Ground",
          price: 8.5,
          estimatedDelivery: "3-5 business days",
     },
     {
          carrier: "UPS",
          serviceLevel: "2nd Day Air",
          price: 18.75,
          estimatedDelivery: "2 business days",
     },
     {
          carrier: "FedEx",
          serviceLevel: "Express",
          price: 15.0,
          estimatedDelivery: "1-2 business days",
     },
     {
          carrier: "FedEx",
          serviceLevel: "Ground",
          price: 7.99,
          estimatedDelivery: "4-6 business days",
     },
];

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
          onOpenChange(false);
     };

     return (
          <Dialog open={open} onOpenChange={handleClose}>
               <DialogContent className="sm:max-w-[500px]">
                    <DialogHeader>
                         <DialogTitle className="flex items-center gap-2">
                              <DollarSign className="h-5 w-5" />
                              Select Shipping Rate
                         </DialogTitle>
                         <DialogDescription>
                              Choose a carrier and service level for this shipment. Rates are estimates and may vary
                              based on package dimensions.
                         </DialogDescription>
                    </DialogHeader>

                    <div className="py-4">
                         <RadioGroup value={selectedRate ?? ""} onValueChange={setSelectedRate} className="space-y-3">
                              {FAKE_RATES.map((rate) => {
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

                    <DialogFooter>
                         <Button variant="outline" onClick={handleClose}>
                              Cancel
                         </Button>
                         <Button onClick={handleSelectRate} disabled={!selectedRate || isPending}>
                              {isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                              Select Rate
                         </Button>
                    </DialogFooter>
               </DialogContent>
          </Dialog>
     );
}
