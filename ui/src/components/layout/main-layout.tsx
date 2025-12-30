import { Sidebar } from "./sidebar";
import { Header } from "./header";

interface MainLayoutProps {
  children: React.ReactNode;
}

/**
 * Main layout wrapper for authenticated pages.
 * In demo mode, no authentication is required.
 */
export function MainLayout({ children }: MainLayoutProps) {
  // Demo user for display purposes
  const demoUser = {
    name: "Demo User",
    email: "demo@tempest.local",
    image: null,
  };

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header user={demoUser} />
        <main className="flex-1 overflow-auto p-6">{children}</main>
      </div>
    </div>
  );
}
