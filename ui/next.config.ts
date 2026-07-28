import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Enable standalone output for Docker deployment
  // This creates a minimal production build with all dependencies bundled
  output: "standalone",

  // Keep the Temporal client (and its native @grpc/grpc-js deps) out of the
  // bundler so it runs as a normal Node module on the server and its files are
  // traced into .next/standalone.
  serverExternalPackages: ["@temporalio/client"],
};

export default nextConfig;
