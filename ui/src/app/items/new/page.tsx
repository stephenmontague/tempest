"use client";

import { MainLayout } from "@/components/layout";
import { PageHeader } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ArrowLeft, Loader2 } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";
import { createItem } from "@/app/actions/items";
import { toast } from "sonner";

export default function NewItemPage() {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  const [sku, setSku] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!sku.trim()) {
      toast.error("SKU is required");
      return;
    }

    if (!name.trim()) {
      toast.error("Name is required");
      return;
    }

    startTransition(async () => {
      const result = await createItem({
        sku: sku.trim(),
        name: name.trim(),
        description: description.trim() || undefined,
      });

      if (result.success && result.data) {
        toast.success("Item created successfully");
        router.push(`/items/${result.data.itemId}`);
      } else {
        toast.error("Failed to create item", {
          description: result.error,
        });
      }
    });
  };

  return (
    <MainLayout>
      <PageHeader title="Add New Item" description="Add a new item to your catalog">
        <Button variant="outline" asChild>
          <Link href="/items">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Items
          </Link>
        </Button>
      </PageHeader>

      <Card className="max-w-2xl">
        <CardHeader>
          <CardTitle>Item Details</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="sku">SKU *</Label>
              <Input
                id="sku"
                value={sku}
                onChange={(e) => setSku(e.target.value)}
                placeholder="e.g., WIDGET-001"
                className="font-mono"
                required
              />
              <p className="text-xs text-muted-foreground">
                Unique identifier for this item
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="name">Name *</Label>
              <Input
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g., Blue Widget"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Optional description of the item..."
                rows={4}
              />
            </div>

            <div className="flex justify-end gap-4">
              <Button type="button" variant="outline" asChild>
                <Link href="/items">Cancel</Link>
              </Button>
              <Button type="submit" disabled={isPending}>
                {isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                Create Item
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </MainLayout>
  );
}

