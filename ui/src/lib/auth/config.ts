/**
 * Auth configuration stub for Tempest UI.
 * Currently configured with no authentication for demo/development purposes.
 * 
 * When you're ready to add authentication, replace this with NextAuth config.
 */

// Export empty handlers for compatibility
export const handlers = {
  GET: async () => new Response("Auth disabled", { status: 200 }),
  POST: async () => new Response("Auth disabled", { status: 200 }),
};

// Export no-op functions for compatibility
export const auth = async () => null;
export const signIn = async () => {};
export const signOut = async () => {};
