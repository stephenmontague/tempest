import { MainLayout } from "@/components/layout";
import { PageHeader } from "@/components/shared";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { requireSession } from "@/lib/auth/session";
import Link from "next/link";
import { Workflow } from "lucide-react";

export default async function SettingsPage() {
  const session = await requireSession();

  return (
    <MainLayout>
      <PageHeader
        title="Settings"
        description="Manage your account and preferences"
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Account</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <div className="text-sm text-muted-foreground">Name</div>
              <div className="font-medium">{session.user.name || "—"}</div>
            </div>
            <div>
              <div className="text-sm text-muted-foreground">Email</div>
              <div>{session.user.email || "—"}</div>
            </div>
            <div>
              <div className="text-sm text-muted-foreground">User ID</div>
              <div className="font-mono text-sm">{session.user.id}</div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Tenant</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <div className="text-sm text-muted-foreground">Tenant ID</div>
              <div className="font-mono">{session.user.tenantId}</div>
            </div>
            <div>
              <div className="text-sm text-muted-foreground">Roles</div>
              <div className="flex flex-wrap gap-2 mt-1">
                {session.user.roles.length > 0 ? (
                  session.user.roles.map((role) => (
                    <span
                      key={role}
                      className="px-2 py-1 bg-muted rounded text-xs font-mono"
                    >
                      {role}
                    </span>
                  ))
                ) : (
                  <span className="text-muted-foreground">No roles assigned</span>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Demo & Tools</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-3">
              <Link href="/settings/random-dag-demo">
                <Button variant="outline" className="w-full justify-start" size="lg">
                  <Workflow className="h-5 w-5 mr-3" />
                  <div className="flex flex-col items-start">
                    <div className="font-semibold">Random DAG Demo</div>
                    <div className="text-xs text-muted-foreground font-normal">
                      Interactive DAG editor - reorder workflow steps and execute with Temporal
                    </div>
                  </div>
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </MainLayout>
  );
}

