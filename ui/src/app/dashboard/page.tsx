import { MainLayout } from "@/components/layout";
import { PageHeader, DataCard } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ShoppingCart, Package, Layers, Truck, Plus, ArrowRight, Activity, Inbox } from "lucide-react";
import Link from "next/link";
import { getOmsClient } from "@/services/oms-client";
import { getImsClient } from "@/services/ims-client";
import { getWmsClient } from "@/services/wms-client";
import { getSmsClient } from "@/services/sms-client";

// Force dynamic rendering - dashboard data should always be fresh
export const dynamic = "force-dynamic";

interface DashboardStats {
     orders: { total: number; awaitingWave: number; inProgress: number };
     items: { total: number; active: number };
     waves: { created: number; inProgress: number };
     shipments: { pending: number; inTransit: number };
}

async function fetchDashboardStats(): Promise<DashboardStats> {
     const stats: DashboardStats = {
          orders: { total: 0, awaitingWave: 0, inProgress: 0 },
          items: { total: 0, active: 0 },
          waves: { created: 0, inProgress: 0 },
          shipments: { pending: 0, inTransit: 0 },
     };

     try {
          const omsClient = getOmsClient();
          const orders = await omsClient.getOrders();
          stats.orders.total = orders.length;
          stats.orders.awaitingWave = orders.filter((o) => o.status === "AWAITING_WAVE").length;
          stats.orders.inProgress = orders.filter((o) => ["RESERVED", "PICKING", "PACKING"].includes(o.status)).length;
     } catch {
          // Service unavailable - use defaults
     }

     try {
          const imsClient = getImsClient();
          const items = await imsClient.getItems();
          stats.items.total = items.length;
          stats.items.active = items.filter((i) => i.active).length;
     } catch {
          // Service unavailable - use defaults
     }

     try {
          const wmsClient = getWmsClient();
          const waves = await wmsClient.getWaves();
          stats.waves.created = waves.filter((w) => w.status === "CREATED").length;
          stats.waves.inProgress = waves.filter((w) => ["RELEASED", "IN_PROGRESS"].includes(w.status)).length;
     } catch {
          // Service unavailable - use defaults
     }

     try {
          const smsClient = getSmsClient();
          const shipments = await smsClient.getShipments();
          stats.shipments.pending = shipments.filter((s) => s.status === "PENDING").length;
          stats.shipments.inTransit = shipments.filter((s) => s.status === "IN_TRANSIT").length;
     } catch {
          // Service unavailable - use defaults
     }

     return stats;
}

export default async function DashboardPage() {
     const stats = await fetchDashboardStats();

     const hasNoData =
          stats.orders.total === 0 &&
          stats.items.total === 0 &&
          stats.waves.created === 0 &&
          stats.waves.inProgress === 0;

     return (
          <MainLayout>
               <PageHeader title="Dashboard" description="Overview of your warehouse operations">
                    <Button asChild>
                         <Link href="/orders/new">
                              <Plus className="h-4 w-4 mr-2" />
                              New Order
                         </Link>
                    </Button>
               </PageHeader>

               {/* Stats Grid */}
               <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6">
                    <DataCard
                         title="Total Orders"
                         value={stats.orders.total}
                         description={`${stats.orders.awaitingWave} awaiting wave`}
                         icon={ShoppingCart}
                    />
                    <DataCard
                         title="Active Items"
                         value={stats.items.active}
                         description={`${stats.items.total} total in catalog`}
                         icon={Package}
                    />
                    <DataCard
                         title="Active Waves"
                         value={stats.waves.inProgress}
                         description={`${stats.waves.created} created, ready to release`}
                         icon={Layers}
                    />
                    <DataCard
                         title="Pending Shipments"
                         value={stats.shipments.pending}
                         description={`${stats.shipments.inTransit} in transit`}
                         icon={Truck}
                    />
               </div>

               <div className="grid gap-6 lg:grid-cols-2">
                    {/* Quick Actions */}
                    <Card>
                         <CardHeader>
                              <CardTitle className="text-lg">Quick Actions</CardTitle>
                         </CardHeader>
                         <CardContent className="grid gap-3">
                              <Button variant="outline" className="justify-between" asChild>
                                   <Link href="/orders/new">
                                        <span className="flex items-center gap-2">
                                             <ShoppingCart className="h-4 w-4" />
                                             Create New Order
                                        </span>
                                        <ArrowRight className="h-4 w-4" />
                                   </Link>
                              </Button>
                              <Button variant="outline" className="justify-between" asChild>
                                   <Link href="/waves/new">
                                        <span className="flex items-center gap-2">
                                             <Layers className="h-4 w-4" />
                                             Create Wave
                                        </span>
                                        <ArrowRight className="h-4 w-4" />
                                   </Link>
                              </Button>
                              <Button variant="outline" className="justify-between" asChild>
                                   <Link href="/items/new">
                                        <span className="flex items-center gap-2">
                                             <Package className="h-4 w-4" />
                                             Add New Item
                                        </span>
                                        <ArrowRight className="h-4 w-4" />
                                   </Link>
                              </Button>
                              <Button variant="outline" className="justify-between" asChild>
                                   <Link href="/orders?status=AWAITING_WAVE">
                                        <span className="flex items-center gap-2">
                                             <Activity className="h-4 w-4" />
                                             View Orders Awaiting Wave
                                        </span>
                                        <ArrowRight className="h-4 w-4" />
                                   </Link>
                              </Button>
                         </CardContent>
                    </Card>

                    {/* Getting Started / Status */}
                    <Card>
                         <CardHeader>
                              <CardTitle className="text-lg">
                                   {hasNoData ? "Getting Started" : "System Status"}
                              </CardTitle>
                         </CardHeader>
                         <CardContent>
                              {hasNoData ? (
                                   <div className="space-y-4">
                                        <div className="flex items-start gap-3 text-sm">
                                             <div className="mt-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-muted text-xs font-medium">
                                                  1
                                             </div>
                                             <div>
                                                  <p className="font-medium">Add items to your catalog</p>
                                                  <p className="text-muted-foreground">
                                                       Create SKUs for products you&apos;ll be fulfilling
                                                  </p>
                                             </div>
                                        </div>
                                        <div className="flex items-start gap-3 text-sm">
                                             <div className="mt-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-muted text-xs font-medium">
                                                  2
                                             </div>
                                             <div>
                                                  <p className="font-medium">Create orders</p>
                                                  <p className="text-muted-foreground">
                                                       Submit orders to trigger the intake workflow
                                                  </p>
                                             </div>
                                        </div>
                                        <div className="flex items-start gap-3 text-sm">
                                             <div className="mt-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-muted text-xs font-medium">
                                                  3
                                             </div>
                                             <div>
                                                  <p className="font-medium">Create and release waves</p>
                                                  <p className="text-muted-foreground">
                                                       Batch orders into waves for fulfillment
                                                  </p>
                                             </div>
                                        </div>
                                        <div className="flex items-start gap-3 text-sm">
                                             <div className="mt-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-muted text-xs font-medium">
                                                  4
                                             </div>
                                             <div>
                                                  <p className="font-medium">Signal completion</p>
                                                  <p className="text-muted-foreground">
                                                       Mark picks and packs complete to progress workflows
                                                  </p>
                                             </div>
                                        </div>
                                   </div>
                              ) : (
                                   <div className="flex flex-col items-center justify-center py-6 text-center">
                                        <Inbox className="h-10 w-10 text-muted-foreground mb-3" />
                                        <p className="text-sm text-muted-foreground">
                                             Backend services connected. View activity in the respective sections.
                                        </p>
                                   </div>
                              )}
                         </CardContent>
                    </Card>
               </div>

               {/* Workflow Status Overview */}
               <Card className="mt-6">
                    <CardHeader>
                         <CardTitle className="text-lg flex items-center gap-2">
                              <Activity className="h-5 w-5" />
                              Active Workflows
                         </CardTitle>
                    </CardHeader>
                    <CardContent>
                         <div className="grid gap-4 md:grid-cols-3">
                              <div className="p-4 rounded-lg bg-muted/50 border">
                                   <div className="text-2xl font-bold font-mono text-primary">
                                        {stats.orders.inProgress}
                                   </div>
                                   <div className="text-sm text-muted-foreground mt-1">Orders In Fulfillment</div>
                                   <Button variant="link" className="px-0 h-auto mt-2" asChild>
                                        <Link href="/orders?status=RESERVED">View all →</Link>
                                   </Button>
                              </div>
                              <div className="p-4 rounded-lg bg-muted/50 border">
                                   <div className="text-2xl font-bold font-mono text-accent">
                                        {stats.waves.inProgress}
                                   </div>
                                   <div className="text-sm text-muted-foreground mt-1">Wave Execution Workflows</div>
                                   <Button variant="link" className="px-0 h-auto mt-2" asChild>
                                        <Link href="/waves?status=RELEASED">View all →</Link>
                                   </Button>
                              </div>
                              <div className="p-4 rounded-lg bg-muted/50 border">
                                   <div className="text-2xl font-bold font-mono text-success">
                                        {stats.shipments.pending}
                                   </div>
                                   <div className="text-sm text-muted-foreground mt-1">Pending Shipments</div>
                                   <Button variant="link" className="px-0 h-auto mt-2" asChild>
                                        <Link href="/shipments?status=PENDING">View all →</Link>
                                   </Button>
                              </div>
                         </div>
                    </CardContent>
               </Card>
          </MainLayout>
     );
}
