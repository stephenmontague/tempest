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
import { ArrowLeft, Truck, Package, ExternalLink } from "lucide-react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { getSmsClient, Parcel } from "@/services/sms-client";

interface ShipmentDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function ShipmentDetailPage({ params }: ShipmentDetailPageProps) {
  const { id } = await params;
  const shipmentId = parseInt(id, 10);

  if (isNaN(shipmentId)) {
    notFound();
  }

  const client = getSmsClient();

  let shipment;
  let parcels: Parcel[] = [];
  try {
    shipment = await client.getShipment(shipmentId);
  } catch {
    notFound();
  }

  // Parcels are optional - fetch separately so shipment page still works if parcels endpoint is unavailable
  try {
    parcels = await client.getParcels(shipmentId);
  } catch {
    // Parcels endpoint may not exist yet - that's okay
    parcels = [];
  }

  return (
    <MainLayout>
      <PageHeader
        title={`Shipment #${shipment.id}`}
        description={`Order #${shipment.orderId} • ${shipment.carrier} ${shipment.serviceLevel}`}
      >
        <Button variant="outline" asChild>
          <Link href="/shipments">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Shipments
          </Link>
        </Button>
      </PageHeader>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Shipment Info */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Truck className="h-5 w-5" />
                Shipment Details
              </CardTitle>
              <StatusBadge status={shipment.status} />
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <div className="text-sm text-muted-foreground">Shipment ID</div>
                  <div className="font-mono">#{shipment.id}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Order</div>
                  <Link
                    href={`/orders/${shipment.orderId}`}
                    className="font-mono text-primary hover:underline"
                  >
                    #{shipment.orderId}
                  </Link>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Carrier</div>
                  <div className="font-medium">{shipment.carrier}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Service Level</div>
                  <div>{shipment.serviceLevel}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Facility</div>
                  <div className="font-mono">#{shipment.facilityId}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Tracking Number</div>
                  <div className="font-mono">
                    {shipment.trackingNumber || "Not available"}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Parcels */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Package className="h-5 w-5" />
                Parcels
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              {parcels.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground">
                  No parcels in this shipment
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Parcel ID</TableHead>
                      <TableHead>Tracking</TableHead>
                      <TableHead>Weight (oz)</TableHead>
                      <TableHead>Dimensions (in)</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {parcels.map((parcel) => (
                      <TableRow key={parcel.id}>
                        <TableCell className="font-mono text-sm">
                          #{parcel.id}
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          {parcel.trackingNumber || "—"}
                        </TableCell>
                        <TableCell className="font-mono">
                          {parcel.weightOz ?? "—"}
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          {parcel.lengthIn && parcel.widthIn && parcel.heightIn
                            ? `${parcel.lengthIn} × ${parcel.widthIn} × ${parcel.heightIn}`
                            : "—"}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Timeline */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Timeline</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-start gap-3">
                <div className="mt-1 h-2 w-2 rounded-full bg-muted-foreground shrink-0" />
                <div>
                  <div className="text-sm font-medium">Created</div>
                  <div className="text-xs text-muted-foreground">
                    {new Date(shipment.createdAt).toLocaleString()}
                  </div>
                </div>
              </div>

              {shipment.shippedAt && (
                <div className="flex items-start gap-3">
                  <div className="mt-1 h-2 w-2 rounded-full bg-primary shrink-0" />
                  <div>
                    <div className="text-sm font-medium">Shipped</div>
                    <div className="text-xs text-muted-foreground">
                      {new Date(shipment.shippedAt).toLocaleString()}
                    </div>
                  </div>
                </div>
              )}

              {shipment.deliveredAt && (
                <div className="flex items-start gap-3">
                  <div className="mt-1 h-2 w-2 rounded-full bg-success shrink-0" />
                  <div>
                    <div className="text-sm font-medium">Delivered</div>
                    <div className="text-xs text-muted-foreground">
                      {new Date(shipment.deliveredAt).toLocaleString()}
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Label */}
          {shipment.labelUrl && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Shipping Label</CardTitle>
              </CardHeader>
              <CardContent>
                <Button className="w-full" asChild>
                  <a
                    href={shipment.labelUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <ExternalLink className="h-4 w-4 mr-2" />
                    View Label
                  </a>
                </Button>
              </CardContent>
            </Card>
          )}

          {/* Metadata */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Metadata</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Updated</span>
                <span>{new Date(shipment.updatedAt).toLocaleString()}</span>
              </div>
              {shipment.createdByUserId && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Created By</span>
                  <span className="font-mono text-xs">{shipment.createdByUserId}</span>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </MainLayout>
  );
}
