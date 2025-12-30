import { redirect } from "next/navigation";

/**
 * Auth error page - currently redirects to dashboard since auth is disabled for demo mode.
 */
export default function AuthErrorPage() {
  redirect("/dashboard");
}
