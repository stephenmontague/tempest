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
import { ArrowLeft, Package, Edit, ShoppingCart, ExternalLink } from "lucide-react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { getImsClient } from "@/services/ims-client";
import { getOmsClient, Order } from "@/services/oms-client";

interface ItemDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function ItemDetailPage({ params }: ItemDetailPageProps) {
  const { id } = await params;
  const itemId = parseInt(id, 10);

  if (isNaN(itemId)) {
    notFound();
  }

  const imsClient = getImsClient();
  const omsClient = getOmsClient();

  let item;
  try {
    item = await imsClient.getItem(itemId);
  } catch {
    notFound();
  }

  // Fetch orders that contain this item's SKU
  let relatedOrders: Order[] = [];
  try {
    relatedOrders = await omsClient.getOrdersBySku(item.sku);
  } catch {
    // Keep empty array on error
  }

  return (
    <MainLayout>
      <PageHeader
        title={item.name}
        description={`SKU: ${item.sku}`}
      >
        <div className="flex gap-2">
          <Button variant="outline" asChild>
            <Link href="/items">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Items
            </Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href={`/items/${item.id}/edit`}>
              <Edit className="h-4 w-4 mr-2" />
              Edit
            </Link>
          </Button>
        </div>
      </PageHeader>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Item Details */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Package className="h-5 w-5" />
                Item Details
              </CardTitle>
              <StatusBadge status={item.active ? "ACTIVE" : "INACTIVE"} />
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <div className="text-sm text-muted-foreground">Item ID</div>
                  <div className="font-mono">#{item.id}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">SKU</div>
                  <div className="font-mono">{item.sku}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Name</div>
                  <div className="font-medium">{item.name}</div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">Status</div>
                  <div>{item.active ? "Active" : "Inactive"}</div>
                </div>
              </div>

              {item.description && (
                <div>
                  <div className="text-sm text-muted-foreground mb-1">Description</div>
                  <p className="text-sm">{item.description}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Related Orders */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShoppingCart className="h-5 w-5" />
                Related Orders
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              {relatedOrders.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Order ID</TableHead>
                      <TableHead>External ID</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Created</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {relatedOrders.map((order) => (
                      <TableRow key={order.id}>
                        <TableCell>
                          <Link
                            href={`/orders/${order.id}`}
                            className="inline-flex items-center gap-1 font-mono text-primary hover:underline"
                          >
                            #{order.id}
                            <ExternalLink className="h-3 w-3" />
                          </Link>
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          {order.externalOrderId}
                        </TableCell>
                        <TableCell>
                          <StatusBadge status={order.status} />
                        </TableCell>
                        <TableCell className="text-sm text-muted-foreground">
                          {new Date(order.createdAt).toLocaleDateString()}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <div className="p-6 text-center text-muted-foreground">
                  No orders contain this item
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Metadata */}
          <Card>
            <CardHeader>
              <CardTitle>Metadata</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <div className="text-sm text-muted-foreground">Created</div>
                <div>{new Date(item.createdAt).toLocaleString()}</div>
              </div>
              <div>
                <div className="text-sm text-muted-foreground">Updated</div>
                <div>{new Date(item.updatedAt).toLocaleString()}</div>
              </div>
              {item.createdByUserId && (
                <div>
                  <div className="text-sm text-muted-foreground">Created By</div>
                  <div className="font-mono text-sm">{item.createdByUserId}</div>
                </div>
              )}
              {item.updatedByUserId && (
                <div>
                  <div className="text-sm text-muted-foreground">Updated By</div>
                  <div className="font-mono text-sm">{item.updatedByUserId}</div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </MainLayout>
  );
}
