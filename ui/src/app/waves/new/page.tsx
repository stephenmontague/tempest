"use client";

import { MainLayout } from "@/components/layout";
import { PageHeader, StatusBadge } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ArrowLeft, Loader2 } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, useTransition, useEffect } from "react";
import { createWave } from "@/app/actions/waves";
import { toast } from "sonner";

interface Order {
  id: number;
  externalOrderId: string;
  customerName?: string;
  status: string;
  createdAt: string;
}

export default function NewWavePage() {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [isLoading, setIsLoading] = useState(true);

  const [waveNumber, setWaveNumber] = useState("");
  const [facilityId, setFacilityId] = useState("1");
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrderIds, setSelectedOrderIds] = useState<number[]>([]);

  // Fetch orders awaiting wave
  useEffect(() => {
    let cancelled = false;
    
    async function fetchOrders() {
      try {
        const response = await fetch("/api/orders?status=AWAITING_WAVE");
        if (response.ok && !cancelled) {
          const data = await response.json();
          setOrders(data);
        }
      } catch (error) {
        if (!cancelled) {
          console.error("Failed to fetch orders:", error);
          toast.error("Failed to load orders");
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }
    fetchOrders();
    
    return () => {
      cancelled = true;
    };
  }, []);

  const toggleOrder = (orderId: number) => {
    setSelectedOrderIds((prev) =>
      prev.includes(orderId)
        ? prev.filter((id) => id !== orderId)
        : [...prev, orderId]
    );
  };

  const toggleAll = () => {
    if (selectedOrderIds.length === orders.length) {
      setSelectedOrderIds([]);
    } else {
      setSelectedOrderIds(orders.map((o) => o.id));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (selectedOrderIds.length === 0) {
      toast.error("Please select at least one order");
      return;
    }

    startTransition(async () => {
      const result = await createWave({
        facilityId: parseInt(facilityId, 10),
        waveNumber: waveNumber.trim() || undefined,
        orderIds: selectedOrderIds,
      });

      if (result.success && result.data) {
        toast.success("Wave created successfully", {
          description: `Wave: ${result.data.waveNumber}`,
        });
        router.push(`/waves/${result.data.waveId}`);
      } else {
        toast.error("Failed to create wave", {
          description: result.error,
        });
      }
    });
  };

  return (
    <MainLayout>
      <PageHeader
        title="Create New Wave"
        description="Select orders to batch process together"
      >
        <Button variant="outline" asChild>
          <Link href="/waves">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Waves
          </Link>
        </Button>
      </PageHeader>

      <form onSubmit={handleSubmit}>
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Wave Settings */}
          <Card>
            <CardHeader>
              <CardTitle>Wave Settings</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="waveNumber">Wave Number (Optional)</Label>
                <Input
                  id="waveNumber"
                  value={waveNumber}
                  onChange={(e) => setWaveNumber(e.target.value)}
                  placeholder="Auto-generated if empty"
                  className="font-mono"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="facilityId">Facility ID</Label>
                <Input
                  id="facilityId"
                  type="number"
                  value={facilityId}
                  onChange={(e) => setFacilityId(e.target.value)}
                  min="1"
                  required
                />
              </div>

              <div className="pt-4 border-t">
                <div className="text-sm text-muted-foreground mb-2">
                  Selected Orders
                </div>
                <div className="text-2xl font-bold font-mono">
                  {selectedOrderIds.length}
                </div>
              </div>

              <Button
                type="submit"
                className="w-full"
                disabled={isPending || selectedOrderIds.length === 0}
              >
                {isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                Create Wave
              </Button>
            </CardContent>
          </Card>

          {/* Order Selection */}
          <Card className="lg:col-span-2">
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>Orders Awaiting Wave</CardTitle>
              {orders.length > 0 && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={toggleAll}
                >
                  {selectedOrderIds.length === orders.length
                    ? "Deselect All"
                    : "Select All"}
                </Button>
              )}
            </CardHeader>
            <CardContent className="p-0">
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                </div>
              ) : orders.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  <p>No orders awaiting wave</p>
                  <Button variant="link" asChild className="mt-2">
                    <Link href="/orders/new">Create an order first</Link>
                  </Button>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-12">
                        <Checkbox
                          checked={selectedOrderIds.length === orders.length}
                          onCheckedChange={toggleAll}
                        />
                      </TableHead>
                      <TableHead>Order ID</TableHead>
                      <TableHead>External ID</TableHead>
                      <TableHead>Customer</TableHead>
                      <TableHead>Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {orders.map((order) => (
                      <TableRow
                        key={order.id}
                        className="cursor-pointer"
                        onClick={() => toggleOrder(order.id)}
                      >
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          <Checkbox
                            checked={selectedOrderIds.includes(order.id)}
                            onCheckedChange={() => toggleOrder(order.id)}
                          />
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          #{order.id}
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          {order.externalOrderId}
                        </TableCell>
                        <TableCell>{order.customerName || "—"}</TableCell>
                        <TableCell>
                          <StatusBadge status={order.status} />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </div>
      </form>
    </MainLayout>
  );
}

