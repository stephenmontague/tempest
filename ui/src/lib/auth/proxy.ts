import { NextResponse } from "next/server";
import { getSession, TempestSession } from "./session";

/**
 * Result of authentication check in route handlers.
 */
export type AuthResult =
     | { authenticated: true; session: TempestSession }
     | { authenticated: false; response: NextResponse };

/**
 * Check authentication in a route handler.
 * Returns the session if authenticated, or a 401 response if not.
 *
 * Usage in route handlers:
 * ```
 * export async function GET() {
 *   const authResult = await requireAuth();
 *   if (!authResult.authenticated) {
 *     return authResult.response;
 *   }
 *   const { session } = authResult;
 *   // ... use session.accessToken, session.user.tenantId, etc.
 * }
 * ```
 */
export async function requireAuth(): Promise<AuthResult> {
     const session = await getSession();

     if (!session) {
          return {
               authenticated: false,
               response: NextResponse.json(
                    { error: "Unauthorized", message: "Authentication required" },
                    { status: 401 }
               ),
          };
     }

     return {
          authenticated: true,
          session,
     };
}

/**
 * Check authentication and role authorization in a route handler.
 * Returns the session if authenticated and authorized, or an error response if not.
 *
 * Usage:
 * ```
 * export async function POST() {
 *   const authResult = await requireAuthWithRoles("ADMIN", "MANAGER");
 *   if (!authResult.authenticated) {
 *     return authResult.response;
 *   }
 *   // ... proceed with authorized operation
 * }
 * ```
 */
export async function requireAuthWithRoles(...requiredRoles: string[]): Promise<AuthResult> {
     const session = await getSession();

     if (!session) {
          return {
               authenticated: false,
               response: NextResponse.json(
                    { error: "Unauthorized", message: "Authentication required" },
                    { status: 401 }
               ),
          };
     }

     const userRoles = session.user?.roles ?? [];
     const hasRequiredRole = requiredRoles.some((role) => userRoles.includes(role));

     if (!hasRequiredRole) {
          return {
               authenticated: false,
               response: NextResponse.json(
                    {
                         error: "Forbidden",
                         message: `Requires one of: ${requiredRoles.join(", ")}`,
                    },
                    { status: 403 }
               ),
          };
     }

     return {
          authenticated: true,
          session,
     };
}

/**
 * Create authorization headers for backend service calls.
 * Uses the access token from the session.
 */
export function createAuthHeaders(accessToken: string): HeadersInit {
     return {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
     };
}
