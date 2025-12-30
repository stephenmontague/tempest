import { getSession, TempestSession } from "./session";

/**
 * Result of authentication check in route handlers.
 * In demo mode, always returns authenticated.
 */
export type AuthResult = { authenticated: true; session: TempestSession };

/**
 * Check authentication in a route handler.
 * In demo mode, always returns authenticated with a demo session.
 */
export async function requireAuth(): Promise<AuthResult> {
  const session = await getSession();
  return {
    authenticated: true,
    session,
  };
}

/**
 * Check authentication and role authorization in a route handler.
 * In demo mode, always returns authenticated with a demo session.
 */
export async function requireAuthWithRoles(
  ..._requiredRoles: string[]
): Promise<AuthResult> {
  const session = await getSession();
  return {
    authenticated: true,
    session,
  };
}

/**
 * Create headers for backend service calls.
 * In demo mode, no Authorization header is needed.
 */
export function createAuthHeaders(_accessToken?: string): HeadersInit {
  return {
    "Content-Type": "application/json",
  };
}
