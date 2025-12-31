"use client";

import { MainLayout } from "@/components/layout";
import { PageHeader, ItemCombobox } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Plus, Trash2, ArrowLeft, Loader2 } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";
import { createOrder, OrderIntakeRequest } from "@/app/actions/orders";
import { toast } from "sonner";

interface OrderLine {
  id: string;
  sku: string;
  itemId?: number;
  quantity: number;
  unitPrice?: number;
}

export default function NewOrderPage() {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  const [externalOrderId, setExternalOrderId] = useState("");
  const [customerName, setCustomerName] = useState("");
  const [customerEmail, setCustomerEmail] = useState("");
  const [shippingAddressLine1, setShippingAddressLine1] = useState("");
  const [shippingAddressLine2, setShippingAddressLine2] = useState("");
  const [shippingCity, setShippingCity] = useState("");
  const [shippingState, setShippingState] = useState("");
  const [shippingPostalCode, setShippingPostalCode] = useState("");
  const [shippingCountry, setShippingCountry] = useState("US");

  const [orderLines, setOrderLines] = useState<OrderLine[]>([
    { id: crypto.randomUUID(), sku: "", quantity: 1 },
  ]);

  const addOrderLine = () => {
    setOrderLines([
      ...orderLines,
      { id: crypto.randomUUID(), sku: "", quantity: 1 },
    ]);
  };

  const removeOrderLine = (id: string) => {
    if (orderLines.length > 1) {
      setOrderLines(orderLines.filter((line) => line.id !== id));
    }
  };

  const updateOrderLine = (id: string, field: keyof OrderLine, value: string | number) => {
    setOrderLines(
      orderLines.map((line) =>
        line.id === id ? { ...line, [field]: value } : line
      )
    );
  };

  const updateOrderLineSku = (id: string, sku: string, itemId?: number) => {
    setOrderLines(
      orderLines.map((line) =>
        line.id === id ? { ...line, sku, itemId } : line
      )
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validate
    if (!externalOrderId.trim()) {
      toast.error("External Order ID is required");
      return;
    }

    if (orderLines.some((line) => !line.sku.trim())) {
      toast.error("All order lines must have a SKU");
      return;
    }

    const request: OrderIntakeRequest = {
      externalOrderId: externalOrderId.trim(),
      facilityId: 1, // Default facility for MVP
      customerName: customerName.trim() || undefined,
      customerEmail: customerEmail.trim() || undefined,
      shippingAddressLine1: shippingAddressLine1.trim() || undefined,
      shippingAddressLine2: shippingAddressLine2.trim() || undefined,
      shippingCity: shippingCity.trim() || undefined,
      shippingState: shippingState.trim() || undefined,
      shippingPostalCode: shippingPostalCode.trim() || undefined,
      shippingCountry: shippingCountry.trim() || undefined,
      lines: orderLines.map((line) => ({
        sku: line.sku.trim(),
        quantity: line.quantity,
        unitPrice: line.unitPrice,
      })),
    };

    startTransition(async () => {
      const result = await createOrder(request);

      if (result.success && result.data) {
        toast.success(`Order created successfully`, {
          description: `Order #${result.data.orderId} - ${result.data.externalOrderId}`,
        });
        // Redirect to the order detail page
        router.push(`/orders/${result.data.orderId}`);
      } else {
        toast.error("Failed to create order", {
          description: result.error,
        });
      }
    });
  };

  return (
    <MainLayout>
      <PageHeader title="Create New Order" description="Submit a new order for fulfillment">
        <Button variant="outline" asChild>
          <Link href="/orders">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Orders
          </Link>
        </Button>
      </PageHeader>

      <form onSubmit={handleSubmit}>
        <div className="grid gap-6 lg:grid-cols-2">
          {/* Order Details */}
          <Card>
            <CardHeader>
              <CardTitle>Order Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="externalOrderId">External Order ID *</Label>
                <Input
                  id="externalOrderId"
                  value={externalOrderId}
                  onChange={(e) => setExternalOrderId(e.target.value)}
                  placeholder="e.g., ORD-2024-001"
                  required
                />
                <p className="text-xs text-muted-foreground">
                  Your system&apos;s order identifier
                </p>
              </div>

              <Separator />

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="customerName">Customer Name</Label>
                  <Input
                    id="customerName"
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    placeholder="John Doe"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="customerEmail">Customer Email</Label>
                  <Input
                    id="customerEmail"
                    type="email"
                    value={customerEmail}
                    onChange={(e) => setCustomerEmail(e.target.value)}
                    placeholder="john@example.com"
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Shipping Address */}
          <Card>
            <CardHeader>
              <CardTitle>Shipping Address</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="addressLine1">Address Line 1</Label>
                <Input
                  id="addressLine1"
                  value={shippingAddressLine1}
                  onChange={(e) => setShippingAddressLine1(e.target.value)}
                  placeholder="123 Main St"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="addressLine2">Address Line 2</Label>
                <Input
                  id="addressLine2"
                  value={shippingAddressLine2}
                  onChange={(e) => setShippingAddressLine2(e.target.value)}
                  placeholder="Apt 4B"
                />
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="city">City</Label>
                  <Input
                    id="city"
                    value={shippingCity}
                    onChange={(e) => setShippingCity(e.target.value)}
                    placeholder="New York"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="state">State</Label>
                  <Input
                    id="state"
                    value={shippingState}
                    onChange={(e) => setShippingState(e.target.value)}
                    placeholder="NY"
                  />
                </div>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="postalCode">Postal Code</Label>
                  <Input
                    id="postalCode"
                    value={shippingPostalCode}
                    onChange={(e) => setShippingPostalCode(e.target.value)}
                    placeholder="10001"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="country">Country</Label>
                  <Input
                    id="country"
                    value={shippingCountry}
                    onChange={(e) => setShippingCountry(e.target.value)}
                    placeholder="US"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Order Lines */}
        <Card className="mt-6">
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Order Lines</CardTitle>
            <Button type="button" variant="outline" size="sm" onClick={addOrderLine}>
              <Plus className="h-4 w-4 mr-2" />
              Add Line
            </Button>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {orderLines.map((line) => (
                <div
                  key={line.id}
                  className="grid grid-cols-[1fr_auto_auto_auto] gap-4 items-end p-4 rounded-lg bg-muted/50"
                >
                  <div className="min-w-0 space-y-2">
                    <Label>Item *</Label>
                    <ItemCombobox
                      value={line.sku}
                      onChange={(sku, itemId) =>
                        updateOrderLineSku(line.id, sku, itemId)
                      }
                      placeholder="Select an item..."
                    />
                  </div>
                  <div className="w-28 space-y-2">
                    <Label>Quantity *</Label>
                    <Input
                      type="number"
                      min="1"
                      value={line.quantity}
                      onChange={(e) =>
                        updateOrderLine(line.id, "quantity", parseInt(e.target.value) || 1)
                      }
                      required
                    />
                  </div>
                  <div className="w-28 space-y-2">
                    <Label>Unit Price</Label>
                    <Input
                      type="number"
                      step="0.01"
                      min="0"
                      value={line.unitPrice ?? ""}
                      onChange={(e) =>
                        updateOrderLine(
                          line.id,
                          "unitPrice",
                          e.target.value ? parseFloat(e.target.value) : 0
                        )
                      }
                      placeholder="0.00"
                    />
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => removeOrderLine(line.id)}
                    disabled={orderLines.length === 1}
                    className="shrink-0"
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Submit */}
        <div className="flex justify-end gap-4 mt-6">
          <Button type="button" variant="outline" asChild>
            <Link href="/orders">Cancel</Link>
          </Button>
          <Button type="submit" disabled={isPending}>
            {isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
            Create Order
          </Button>
        </div>
      </form>
    </MainLayout>
  );
}

