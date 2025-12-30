import { MainLayout } from "@/components/layout";
import { PageHeader } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import Link from "next/link";
import { getOmsClient, Order } from "@/services/oms-client";
import { OrdersList } from "./orders-list";

async function getOrders(status?: string): Promise<Order[]> {
  const client = getOmsClient();
  try {
    if (status) {
      return await client.getOrdersByStatus(status);
    }
    return await client.getOrders();
  } catch (error) {
    console.error("Failed to fetch orders:", error);
    return [];
  }
}

interface OrdersPageProps {
  searchParams: Promise<{ status?: string }>;
}

export default async function OrdersPage({ searchParams }: OrdersPageProps) {
  const params = await searchParams;
  const orders = await getOrders(params.status);

  const statusFilters = [
    { label: "All", value: undefined },
    { label: "Created", value: "CREATED" },
    { label: "Awaiting Wave", value: "AWAITING_WAVE" },
    { label: "Reserved", value: "RESERVED" },
    { label: "Picking", value: "PICKING" },
    { label: "Packing", value: "PACKING" },
    { label: "Shipped", value: "SHIPPED" },
  ];

  return (
    <MainLayout>
      <PageHeader
        title="Orders"
        description="Manage customer orders and track fulfillment"
      >
        <Button asChild>
          <Link href="/orders/new">
            <Plus className="h-4 w-4 mr-2" />
            New Order
          </Link>
        </Button>
      </PageHeader>

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
                  ? `/orders?status=${filter.value}`
                  : "/orders"
              }
            >
              {filter.label}
            </Link>
          </Button>
        ))}
      </div>

      {/* Orders list */}
      <OrdersList orders={orders} statusFilter={params.status} />
    </MainLayout>
  );
}
