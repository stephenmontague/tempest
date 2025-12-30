import { redirect } from "next/navigation";

/**
 * Sign-in page - currently redirects to dashboard since auth is disabled for demo mode.
 */
export default function SignInPage() {
  redirect("/dashboard");
}
