/**
 * Session management for Tempest UI.
 * Currently configured with no authentication for demo/development purposes.
 */

/**
 * Session type for the application.
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
 * Create a mock session for demo mode.
 */
function createDemoSession(): TempestSession {
  return {
    user: {
      id: "demo-user-001",
      name: "Demo User",
      email: "demo@tempest.local",
      image: null,
      tenantId: "demo-tenant",
      roles: ["ADMIN", "MANAGER"],
    },
    accessToken: "demo-access-token",
    expires: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
  };
}

/**
 * Get the current session.
 * Always returns a demo session (no auth required).
 */
export async function getSession(): Promise<TempestSession> {
  return createDemoSession();
}

/**
 * Get the current session or redirect to sign in.
 * In demo mode, always returns a session (no redirect needed).
 */
export async function requireSession(): Promise<TempestSession> {
  return createDemoSession();
}

/**
 * Get the access token from the current session.
 */
export async function getAccessToken(): Promise<string> {
  const session = await getSession();
  return session.accessToken;
}

/**
 * Get the tenant ID from the current session.
 */
export async function getTenantId(): Promise<string> {
  const session = await getSession();
  return session.user.tenantId;
}

/**
 * Check if the current user has a specific role.
 * In demo mode, always returns true for common roles.
 */
export async function hasRole(role: string): Promise<boolean> {
  const session = await getSession();
  return session.user.roles.includes(role);
}

/**
 * Check if the current user has any of the specified roles.
 */
export async function hasAnyRole(...roles: string[]): Promise<boolean> {
  const session = await getSession();
  return roles.some((role) => session.user.roles.includes(role));
}
