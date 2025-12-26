import { signIn } from "@/lib/auth/config";

export default function SignInPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="w-full max-w-md">
        <h1 className="text-3xl font-bold text-center mb-8">Sign In</h1>
        <p className="text-center mb-8 text-gray-600">
          Sign in to access the Tempest warehouse management system.
        </p>

        <form
          action={async () => {
            "use server";
            await signIn("oidc", { redirectTo: "/" });
          }}
        >
          <button
            type="submit"
            className="w-full px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Sign in with your organization
          </button>
        </form>
      </div>
    </main>
  );
}

