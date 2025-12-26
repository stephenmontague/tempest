import NextAuth from "next-auth";
import type { NextAuthConfig } from "next-auth";

/**
 * NextAuth configuration for OIDC authentication.
 * Supports multiple IdPs (Keycloak, Auth0, etc.) via environment variables.
 *
 * Required environment variables:
 * - AUTH_SECRET: Secret for session encryption
 * - OIDC_ISSUER: IdP issuer URI (e.g., https://idp.example.com)
 * - OIDC_CLIENT_ID: OAuth2 client ID
 * - OIDC_CLIENT_SECRET: OAuth2 client secret
 */

const config: NextAuthConfig = {
     providers: [
          {
               id: "oidc",
               name: "OIDC Provider",
               type: "oidc",
               issuer: process.env.OIDC_ISSUER,
               clientId: process.env.OIDC_CLIENT_ID,
               clientSecret: process.env.OIDC_CLIENT_SECRET,
               authorization: {
                    params: {
                         scope: "openid profile email",
                    },
               },
               profile(profile) {
                    return {
                         id: profile.sub,
                         name: profile.name || profile.preferred_username,
                         email: profile.email,
                         tenantId: profile.tenant_id,
                         roles: profile.roles || [],
                    };
               },
          },
     ],
     callbacks: {
          async jwt({ token, account, profile }) {
               // On initial sign in, persist the access token and custom claims
               if (account && profile) {
                    token.accessToken = account.access_token;
                    token.tenantId = (profile as { tenant_id?: string }).tenant_id;
                    token.roles = (profile as { roles?: string[] }).roles || [];
               }
               return token;
          },
          async session({ session, token }) {
               // Make access token and custom claims available in the session
               return {
                    ...session,
                    accessToken: token.accessToken as string,
                    user: {
                         ...session.user,
                         id: token.sub,
                         tenantId: token.tenantId as string,
                         roles: token.roles as string[],
                    },
               };
          },
     },
     pages: {
          signIn: "/auth/signin",
          error: "/auth/error",
     },
     session: {
          strategy: "jwt",
          maxAge: 30 * 60, // 30 minutes
     },
     // Cookies are HttpOnly and Secure by default in production
};

export const { handlers, auth, signIn, signOut } = NextAuth(config);
