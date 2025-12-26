import { auth } from "./config";
import { redirect } from "next/navigation";

/**
 * Extended session type with custom claims from JWT.
 */
export interface TempestSession {
     user: {
          id: string;
          name?: string | null;
          email?: string | null;
          image?: string | null;
          tenantId: string;
          roles: string[];
     };
     accessToken: string;
     expires: string;
}

/**
 * Get the current session.
 * Returns null if not authenticated.
 */
export async function getSession(): Promise<TempestSession | null> {
     const session = await auth();
     if (!session) {
          return null;
     }
     return session as TempestSession;
}

/**
 * Get the current session or redirect to sign in.
 * Use this in route handlers that require authentication.
 */
export async function requireSession(): Promise<TempestSession> {
     const session = await getSession();
     if (!session) {
          redirect("/auth/signin");
     }
     return session;
}

/**
 * Get the access token from the current session.
 * Returns null if not authenticated.
 */
export async function getAccessToken(): Promise<string | null> {
     const session = await getSession();
     return session?.accessToken ?? null;
}

/**
 * Get the tenant ID from the current session.
 * Returns null if not authenticated.
 */
export async function getTenantId(): Promise<string | null> {
     const session = await getSession();
     return session?.user?.tenantId ?? null;
}

/**
 * Check if the current user has a specific role.
 */
export async function hasRole(role: string): Promise<boolean> {
     const session = await getSession();
     return session?.user?.roles?.includes(role) ?? false;
}

/**
 * Check if the current user has any of the specified roles.
 */
export async function hasAnyRole(...roles: string[]): Promise<boolean> {
     const session = await getSession();
     const userRoles = session?.user?.roles ?? [];
     return roles.some((role) => userRoles.includes(role));
}
