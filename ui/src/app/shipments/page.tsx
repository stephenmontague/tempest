import { MainLayout } from "@/components/layout";
import { PageHeader, StatusBadge, EmptyState } from "@/components/shared";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardContent } from "@/components/ui/card";
import { Truck, ExternalLink } from "lucide-react";
import Link from "next/link";
import { getSmsClient, Shipment } from "@/services/sms-client";

async function getShipments(status?: string): Promise<Shipment[]> {
  const client = getSmsClient();
  try {
    if (status) {
      return await client.getShipmentsByStatus(status);
    }
    return await client.getShipments();
  } catch (error) {
    console.error("Failed to fetch shipments:", error);
    return [];
  }
}

interface ShipmentsPageProps {
  searchParams: Promise<{ status?: string }>;
}

export default async function ShipmentsPage({ searchParams }: ShipmentsPageProps) {
  const params = await searchParams;
  const shipments = await getShipments(params.status);

  const statusFilters = [
    { label: "All", value: undefined },
    { label: "Pending", value: "PENDING" },
    { label: "Label Created", value: "LABEL_CREATED" },
    { label: "In Transit", value: "IN_TRANSIT" },
    { label: "Delivered", value: "DELIVERED" },
  ];

  return (
    <MainLayout>
      <PageHeader
        title="Shipments"
        description="Track shipments and delivery status"
      />

      {/* Status Filters */}
      <div className="flex gap-2 mb-6 flex-wrap">
        {statusFilters.map((filter) => (
          <Button
            key={filter.label}
            variant={params.status === filter.value ? "default" : "outline"}
            size="sm"
            asChild
          >
            <Link
              href={
                filter.value
                  ? `/shipments?status=${filter.value}`
                  : "/shipments"
              }
            >
              {filter.label}
            </Link>
          </Button>
        ))}
      </div>

      {shipments.length === 0 ? (
        <Card>
          <CardContent className="py-8">
            <EmptyState
              icon={Truck}
              title="No shipments found"
              description={
                params.status
                  ? `No shipments with status "${params.status.replace(/_/g, " ")}"`
                  : "Shipments will appear here once orders are fulfilled"
              }
            />
          </CardContent>
        </Card>
      ) : (
        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Shipment ID</TableHead>
                <TableHead>Order</TableHead>
                <TableHead>Carrier</TableHead>
                <TableHead>Tracking</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Shipped</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {shipments.map((shipment) => (
                <TableRow key={shipment.id}>
                  <TableCell className="font-mono text-sm">
                    #{shipment.id}
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    <Link
                      href={`/orders/${shipment.orderId}`}
                      className="text-primary hover:underline"
                    >
                      #{shipment.orderId}
                    </Link>
                  </TableCell>
                  <TableCell>
                    <div>
                      <div className="font-medium">{shipment.carrier}</div>
                      <div className="text-xs text-muted-foreground">
                        {shipment.serviceLevel}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    {shipment.trackingNumber || "—"}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={shipment.status} />
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {shipment.shippedAt
                      ? new Date(shipment.shippedAt).toLocaleDateString()
                      : "—"}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link href={`/shipments/${shipment.id}`}>
                        <ExternalLink className="h-4 w-4 mr-1" />
                        View
                      </Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      )}
    </MainLayout>
  );
}
