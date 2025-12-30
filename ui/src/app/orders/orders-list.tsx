"use client";

import { StatusBadge, EmptyState } from "@/components/shared";
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
import { Plus, ShoppingCart, ExternalLink } from "lucide-react";
import Link from "next/link";

interface Order {
  id: number;
  externalOrderId: string;
  customerName?: string;
  customerEmail?: string;
  status: string;
  createdAt: string;
}

interface OrdersListProps {
  orders: Order[];
  statusFilter?: string;
}

export function OrdersList({ orders, statusFilter }: OrdersListProps) {
  return (
    <>
      {orders.length === 0 ? (
        <Card>
          <CardContent className="py-8">
            <EmptyState
              icon={ShoppingCart}
              title="No orders found"
              description={
                statusFilter
                  ? `No orders with status "${statusFilter.replace(/_/g, " ")}"`
                  : "Create your first order to get started"
              }
            >
              <Button asChild>
                <Link href="/orders/new">
                  <Plus className="h-4 w-4 mr-2" />
                  Create Order
                </Link>
              </Button>
            </EmptyState>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Order ID</TableHead>
                <TableHead>External ID</TableHead>
                <TableHead>Customer</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
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
                  <TableCell>
                    <div>
                      <div className="font-medium">
                        {order.customerName || "—"}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {order.customerEmail || "No email"}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={order.status} />
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {new Date(order.createdAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link href={`/orders/${order.id}`}>
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
    </>
  );
}
