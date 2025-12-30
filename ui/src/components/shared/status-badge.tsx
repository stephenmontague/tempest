import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

type StatusVariant = "default" | "success" | "warning" | "destructive" | "secondary";

interface StatusBadgeProps {
  status: string;
  variant?: StatusVariant;
  className?: string;
}

// Map common statuses to variants
const statusVariantMap: Record<string, StatusVariant> = {
  // Order statuses
  CREATED: "secondary",
  AWAITING_WAVE: "warning",
  RESERVED: "default",
  PICKING: "default",
  PICKED: "default",
  PACKING: "default",
  PACKED: "default",
  SHIPPED: "success",
  DELIVERED: "success",
  CANCELLED: "destructive",
  FAILED: "destructive",

  // Wave statuses
  RELEASED: "default",
  IN_PROGRESS: "warning",
  COMPLETED: "success",

  // Item statuses
  ACTIVE: "success",
  INACTIVE: "secondary",

  // Shipment statuses
  LABEL_CREATED: "secondary",
  IN_TRANSIT: "warning",

  // Workflow steps
  ALLOCATING: "warning",
  WAITING_FOR_PICKS: "warning",
  WAITING_FOR_PACKS: "warning",
  SHIPPING: "default",
};

export function StatusBadge({ status, variant, className }: StatusBadgeProps) {
  const resolvedVariant = variant ?? statusVariantMap[status] ?? "secondary";

  const variantStyles: Record<StatusVariant, string> = {
    default: "bg-primary/10 text-primary border-primary/20 hover:bg-primary/20",
    success: "bg-success/10 text-success border-success/20 hover:bg-success/20",
    warning: "bg-warning/10 text-warning-foreground border-warning/20 hover:bg-warning/20",
    destructive: "bg-destructive/10 text-destructive border-destructive/20 hover:bg-destructive/20",
    secondary: "bg-muted text-muted-foreground border-muted hover:bg-muted/80",
  };

  return (
    <Badge
      variant="outline"
      className={cn(
        "font-mono text-xs font-medium uppercase tracking-wider",
        variantStyles[resolvedVariant],
        className
      )}
    >
      {status.replace(/_/g, " ")}
    </Badge>
  );
}

