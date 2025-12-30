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
import { Plus, Package, ExternalLink } from "lucide-react";
import Link from "next/link";
import { getImsClient, Item } from "@/services/ims-client";

async function getItems(): Promise<Item[]> {
  const client = getImsClient();
  try {
    return await client.getItems();
  } catch (error) {
    console.error("Failed to fetch items:", error);
    return [];
  }
}

export default async function ItemsPage() {
  const items = await getItems();

  return (
    <MainLayout>
      <PageHeader
        title="Items"
        description="Manage your product catalog and inventory items"
      >
        <Button asChild>
          <Link href="/items/new">
            <Plus className="h-4 w-4 mr-2" />
            New Item
          </Link>
        </Button>
      </PageHeader>

      {items.length === 0 ? (
        <Card>
          <CardContent className="py-8">
            <EmptyState
              icon={Package}
              title="No items found"
              description="Add items to your catalog to start fulfilling orders"
            >
              <Button asChild>
                <Link href="/items/new">
                  <Plus className="h-4 w-4 mr-2" />
                  Add Item
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
                <TableHead>SKU</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {items.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="font-mono text-sm">
                    {item.sku}
                  </TableCell>
                  <TableCell className="font-medium">{item.name}</TableCell>
                  <TableCell className="text-muted-foreground max-w-xs truncate">
                    {item.description || "—"}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={item.active ? "ACTIVE" : "INACTIVE"} />
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {new Date(item.createdAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link href={`/items/${item.id}`}>
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
