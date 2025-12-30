import { MainLayout } from "@/components/layout";
import { PageHeader } from "@/components/shared";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { requireSession } from "@/lib/auth/session";

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
      </div>
    </MainLayout>
  );
}

