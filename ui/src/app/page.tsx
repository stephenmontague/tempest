import { getSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Layers, ArrowRight } from "lucide-react";

export default async function Home() {
  const session = await getSession();

  // If authenticated, redirect to dashboard
  if (session) {
    redirect("/dashboard");
  }

  return (
    <main className="min-h-screen bg-background">
      {/* Hero Section */}
      <div className="relative overflow-hidden">
        {/* Background gradient */}
        <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-background to-accent/5" />

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 sm:py-32">
          <div className="text-center">
            {/* Logo */}
            <div className="flex justify-center mb-8">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary">
                <Layers className="h-8 w-8 text-primary-foreground" />
              </div>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight">
              <span className="text-foreground">Tempest</span>{" "}
              <span className="text-primary">WMS</span>
            </h1>

            <p className="mt-6 text-lg sm:text-xl text-muted-foreground max-w-2xl mx-auto">
              Omnichannel warehouse management system with Temporal workflow
              orchestration. Built for reliability, visibility, and scale.
            </p>

            <div className="mt-10 flex flex-col sm:flex-row gap-4 justify-center">
              <Button size="lg" asChild>
                <Link href="/api/auth/signin">
                  Sign In
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid gap-8 md:grid-cols-3">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Durable Workflows</CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground">
              Powered by Temporal for reliable order fulfillment. Never lose an
              order, even during system failures.
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Wave-Based Fulfillment</CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground">
              Batch orders into waves for efficient warehouse operations.
              Coordinate picks, packs, and shipments.
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Multi-Tenant</CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground">
              Built with tenant isolation from the ground up. Each tenant&apos;s
              data is completely separated.
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <p className="text-center text-sm text-muted-foreground">
            Tempest WMS — Temporal-orchestrated warehouse management
          </p>
        </div>
      </footer>
    </main>
  );
}
