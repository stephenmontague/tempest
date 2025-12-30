import { MainLayout } from "@/components/layout";
import { PageHeader, StatusBadge } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ArrowLeft, Layers, ExternalLink } from "lucide-react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { getWmsClient } from "@/services/wms-client";
import { getOmsClient } from "@/services/oms-client";
import { WaveDetailClient } from "./wave-detail-client";

interface WaveDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function WaveDetailPage({ params }: WaveDetailPageProps) {
  const { id } = await params;
  const waveId = parseInt(id, 10);

  if (isNaN(waveId)) {
    notFound();
  }

  const wmsClient = getWmsClient();
  const omsClient = getOmsClient();

  let wave;
  try {
    wave = await wmsClient.getWave(waveId);
  } catch {
    notFound();
  }

  // Fetch order details for the orders in the wave
  let orders: { id: number; externalOrderId: string; status: string; customerName?: string }[] = [];
  if (wave.orderIds && wave.orderIds.length > 0) {
    try {
      const allOrders = await omsClient.getOrders();
      orders = allOrders.filter((o) => wave.orderIds.includes(o.id));
    } catch {
      // Orders fetch failed, continue without
    }
  }

  const canRelease = wave.status === "CREATED";
  // Show signal buttons based on wave status - workflow status is polled client-side
  // These are initial values; the client component will show/hide based on workflow step
  // Once a wave is RELEASED, it could be in any workflow step, so allow both signals
  const isActiveWave = ["RELEASED", "IN_PROGRESS", "PICKING", "PACKING"].includes(wave.status);
  const canSignalPicks = isActiveWave;
  const canSignalPacks = isActiveWave;
  const canCancel = wave.status !== "COMPLETED" && wave.status !== "CANCELLED";

  return (
    <MainLayout>
      <PageHeader
        title={`Wave ${wave.waveNumber}`}
        description={`Facility #${wave.facilityId} • ${wave.orderIds?.length ?? 0} orders`}
      >
        <Button variant="outline" asChild>
          <Link href="/waves">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Waves
          </Link>
        </Button>
      </PageHeader>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Wave Info */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Layers className="h-5 w-5" />
                Wave Details
              </CardTitle>
              <StatusBadge status={wave.status} />
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <div className="text-sm text-muted-foreground">Wave ID</div>
                  <div className="font-mono">#{wave.id}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Wave Number</div>
                  <div className="font-mono">{wave.waveNumber}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Facility</div>
                  <div className="font-mono">#{wave.facilityId}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Order Count</div>
                  <div className="font-mono">{wave.orderIds?.length ?? 0}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Created</div>
                  <div>{new Date(wave.createdAt).toLocaleString()}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Updated</div>
                  <div>{new Date(wave.updatedAt).toLocaleString()}</div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Orders in Wave */}
          <Card>
            <CardHeader>
              <CardTitle>Orders in Wave</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              {orders.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground">
                  No orders in this wave
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Order ID</TableHead>
                      <TableHead>External ID</TableHead>
                      <TableHead>Customer</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {orders.map((order) => (
                      <TableRow key={order.id}>
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
                        <TableCell className="text-right">
                          <Button variant="ghost" size="sm" asChild>
                            <Link href={`/orders/${order.id}`}>
                              <ExternalLink className="h-4 w-4" />
                            </Link>
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Sidebar - Actions and Status */}
        <div className="space-y-6">
          <WaveDetailClient
            waveId={wave.id}
            workflowId={wave.workflowId}
            initialStatus={wave.status}
            canRelease={canRelease}
            canSignalPicks={canSignalPicks}
            canSignalPacks={canSignalPacks}
            canCancel={canCancel}
            orders={orders}
          />
        </div>
      </div>
    </MainLayout>
  );
}
