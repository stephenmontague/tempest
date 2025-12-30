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
import { ArrowLeft, MapPin, User, Package, ExternalLink } from "lucide-react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { getOmsClient } from "@/services/oms-client";
import { getImsClient } from "@/services/ims-client";
import { OrderDetailClient } from "./order-detail-client";

interface OrderDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function OrderDetailPage({ params }: OrderDetailPageProps) {
  const { id } = await params;
  const orderId = parseInt(id, 10);

  if (isNaN(orderId)) {
    notFound();
  }

  const omsClient = getOmsClient();
  const imsClient = getImsClient();

  let order;
  let orderLines;
  try {
    order = await omsClient.getOrder(orderId);
    orderLines = await omsClient.getOrderLines(orderId);
  } catch {
    notFound();
  }

  // Fetch item IDs for each unique SKU to enable linking
  const uniqueSkus = [...new Set(orderLines.map((line) => line.sku))];
  const skuToItemId: Record<string, number> = {};

  await Promise.all(
    uniqueSkus.map(async (sku) => {
      try {
        const item = await imsClient.getItemBySku(sku);
        if (item) {
          skuToItemId[sku] = item.id;
        }
      } catch {
        // Item not found for this SKU - leave unmapped
      }
    })
  );

  const hasAddress =
    order.shippingAddressLine1 ||
    order.shippingCity ||
    order.shippingState ||
    order.shippingPostalCode;

  return (
    <MainLayout>
      <PageHeader
        title={`Order #${order.id}`}
        description={`External ID: ${order.externalOrderId}`}
      >
        <Button variant="outline" asChild>
          <Link href="/orders">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Orders
          </Link>
        </Button>
      </PageHeader>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Order Info */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Package className="h-5 w-5" />
                Order Details
              </CardTitle>
              <StatusBadge status={order.status} />
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <div className="text-sm text-muted-foreground">Order ID</div>
                  <div className="font-mono">#{order.id}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">External ID</div>
                  <div className="font-mono">{order.externalOrderId}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Created</div>
                  <div>{new Date(order.createdAt).toLocaleString()}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Updated</div>
                  <div>{new Date(order.updatedAt).toLocaleString()}</div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Order Lines */}
          <Card>
            <CardHeader>
              <CardTitle>Order Lines</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>SKU</TableHead>
                    <TableHead className="text-right">Quantity</TableHead>
                    <TableHead className="text-right">Unit Price</TableHead>
                    <TableHead className="text-right">Total</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orderLines.map((line) => {
                    const itemId = skuToItemId[line.sku];
                    return (
                      <TableRow key={line.id}>
                        <TableCell className="font-mono">
                          {itemId ? (
                            <Link
                              href={`/items/${itemId}`}
                              className="inline-flex items-center gap-1 text-primary hover:underline"
                            >
                              {line.sku}
                              <ExternalLink className="h-3 w-3" />
                            </Link>
                          ) : (
                            line.sku
                          )}
                        </TableCell>
                        <TableCell className="text-right font-mono">
                          {line.quantity}
                        </TableCell>
                        <TableCell className="text-right font-mono">
                          {line.unitPrice ? `$${line.unitPrice.toFixed(2)}` : "—"}
                        </TableCell>
                        <TableCell className="text-right font-mono">
                          {line.unitPrice
                            ? `$${(line.unitPrice * line.quantity).toFixed(2)}`
                            : "—"}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Workflow Status - Client Component for Polling */}
          <OrderDetailClient
            orderId={order.id}
            workflowId={order.workflowId}
            initialStatus={order.status}
          />

          {/* Customer Info */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <User className="h-4 w-4" />
                Customer
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <div>
                <div className="text-sm text-muted-foreground">Name</div>
                <div>{order.customerName || "—"}</div>
              </div>
              <div>
                <div className="text-sm text-muted-foreground">Email</div>
                <div>{order.customerEmail || "—"}</div>
              </div>
            </CardContent>
          </Card>

          {/* Shipping Address */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <MapPin className="h-4 w-4" />
                Shipping Address
              </CardTitle>
            </CardHeader>
            <CardContent>
              {hasAddress ? (
                <address className="not-italic text-sm leading-relaxed">
                  {order.shippingAddressLine1 && (
                    <div>{order.shippingAddressLine1}</div>
                  )}
                  {order.shippingAddressLine2 && (
                    <div>{order.shippingAddressLine2}</div>
                  )}
                  {(order.shippingCity || order.shippingState || order.shippingPostalCode) && (
                    <div>
                      {order.shippingCity}
                      {order.shippingCity && order.shippingState && ", "}
                      {order.shippingState} {order.shippingPostalCode}
                    </div>
                  )}
                  {order.shippingCountry && <div>{order.shippingCountry}</div>}
                </address>
              ) : (
                <p className="text-sm text-muted-foreground">No address provided</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </MainLayout>
  );
}
