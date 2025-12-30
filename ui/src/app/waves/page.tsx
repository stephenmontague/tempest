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
import { Plus, Layers, ExternalLink } from "lucide-react";
import Link from "next/link";
import { getWmsClient, Wave } from "@/services/wms-client";

async function getWaves(status?: string): Promise<Wave[]> {
  const client = getWmsClient();
  try {
    if (status) {
      return await client.getWavesByStatus(status);
    }
    return await client.getWaves();
  } catch (error) {
    console.error("Failed to fetch waves:", error);
    return [];
  }
}

interface WavesPageProps {
  searchParams: Promise<{ status?: string }>;
}

export default async function WavesPage({ searchParams }: WavesPageProps) {
  const params = await searchParams;
  const waves = await getWaves(params.status);

  const statusFilters = [
    { label: "All", value: undefined },
    { label: "Created", value: "CREATED" },
    { label: "Released", value: "RELEASED" },
    { label: "In Progress", value: "IN_PROGRESS" },
    { label: "Completed", value: "COMPLETED" },
    { label: "Cancelled", value: "CANCELLED" },
  ];

  return (
    <MainLayout>
      <PageHeader
        title="Waves"
        description="Manage fulfillment waves for batch processing"
      >
        <Button asChild>
          <Link href="/waves/new">
            <Plus className="h-4 w-4 mr-2" />
            New Wave
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
                  ? `/waves?status=${filter.value}`
                  : "/waves"
              }
            >
              {filter.label}
            </Link>
          </Button>
        ))}
      </div>

      {waves.length === 0 ? (
        <Card>
          <CardContent className="py-8">
            <EmptyState
              icon={Layers}
              title="No waves found"
              description={
                params.status
                  ? `No waves with status "${params.status.replace(/_/g, " ")}"`
                  : "Create a wave to batch process orders"
              }
            >
              <Button asChild>
                <Link href="/waves/new">
                  <Plus className="h-4 w-4 mr-2" />
                  Create Wave
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
                <TableHead>Wave Number</TableHead>
                <TableHead>Facility</TableHead>
                <TableHead>Orders</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {waves.map((wave) => (
                <TableRow key={wave.id}>
                  <TableCell className="font-mono text-sm">
                    {wave.waveNumber}
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    #{wave.facilityId}
                  </TableCell>
                  <TableCell className="font-mono">
                    {wave.orderIds?.length ?? 0}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={wave.status} />
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {new Date(wave.createdAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link href={`/waves/${wave.id}`}>
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
