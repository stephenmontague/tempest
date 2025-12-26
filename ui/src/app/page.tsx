import { getSession } from "@/lib/auth/session";
import Link from "next/link";

export default async function Home() {
  const session = await getSession();

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <h1 className="text-4xl font-bold mb-8">Tempest WMS</h1>
      <p className="text-lg mb-8 text-center max-w-xl">
        Omnichannel warehouse management system with multi-tenant support.
      </p>

      {session ? (
        <div className="text-center">
          <p className="mb-4">
            Signed in as <strong>{session.user.email || session.user.name}</strong>
          </p>
          <p className="mb-4 text-sm text-gray-600">
            Tenant: {session.user.tenantId}
          </p>
          <div className="flex gap-4">
            <Link
              href="/dashboard"
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              Go to Dashboard
            </Link>
            <Link
              href="/api/auth/signout"
              className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
            >
              Sign Out
            </Link>
          </div>
        </div>
      ) : (
        <div className="text-center">
          <p className="mb-4">Sign in to access the warehouse management system.</p>
          <Link
            href="/api/auth/signin"
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Sign In
          </Link>
        </div>
      )}
    </main>
  );
}

