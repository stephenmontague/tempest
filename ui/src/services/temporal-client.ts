import "server-only";

import { Client, Connection } from "@temporalio/client";

/**
 * Server-only Temporal client for the Next.js BFF.
 *
 * The UI is the single Temporal *client* tier: it starts workflows, sends signals,
 * and runs queries against Temporal directly. Workers stay in Java, so workflows are
 * started cross-language by their *type-name string* (the Java @WorkflowInterface
 * simple name) on the appropriate per-service task queue.
 *
 * Connection auth mirrors the Java TemporalConfig (local / API-key / mTLS).
 * This module must never be imported into client components — `server-only` enforces it.
 */

const DEFAULT_ADDRESS = "localhost:7233";
const DEFAULT_NAMESPACE = "tempest";

/** Per-service task queues — mirrors Java `TaskQueues`. Never collapse these. */
export const TASK_QUEUES = {
  OMS: "oms-tasks",
  IMS: "ims-tasks",
  WMS: "wms-tasks",
  SMS: "sms-tasks",
} as const;

/**
 * Workflow contracts. `type` must exactly match the Java @WorkflowInterface simple
 * name; `taskQueue` the worker that registers it; `workflowId` the id convention the
 * Java client used (kept identical so ids stay stable).
 */
export const WORKFLOW = {
  randomDag: {
    type: "RandomDAGWorkflow",
    taskQueue: TASK_QUEUES.OMS,
    workflowId: (requestId: string) => `random-dag-${requestId}`,
  },
  waveExecution: {
    type: "WaveExecutionWorkflow",
    taskQueue: TASK_QUEUES.WMS,
    workflowId: (waveId: number | string) => `wave-execution-${waveId}`,
  },
} as const;

function namespace(): string {
  return process.env.TEMPORAL_NAMESPACE ?? DEFAULT_NAMESPACE;
}

let clientPromise: Promise<Client> | null = null;

async function createClient(): Promise<Client> {
  const address = process.env.TEMPORAL_ADDRESS ?? DEFAULT_ADDRESS;
  const apiKey = process.env.TEMPORAL_API_KEY;
  const tlsCert = process.env.TEMPORAL_TLS_CERT;
  const tlsKey = process.env.TEMPORAL_TLS_KEY;

  let connection: Connection;
  if (apiKey && apiKey.length > 0) {
    // API-key auth (Temporal Cloud): TLS + Bearer API key.
    connection = await Connection.connect({ address, tls: true, apiKey });
  } else if (tlsCert && tlsKey) {
    // mTLS auth (Temporal Cloud): client cert + key (PEM strings).
    connection = await Connection.connect({
      address,
      tls: {
        clientCertPair: {
          crt: Buffer.from(tlsCert),
          key: Buffer.from(tlsKey),
        },
      },
    });
  } else {
    // Local development: no auth.
    connection = await Connection.connect({ address });
  }

  return new Client({ connection, namespace: namespace() });
}

/**
 * Returns a memoized Temporal client. If the first connection attempt fails the cache
 * is cleared so a later request can retry.
 */
export function getTemporalClient(): Promise<Client> {
  if (!clientPromise) {
    clientPromise = createClient().catch((err) => {
      clientPromise = null;
      throw err;
    });
  }
  return clientPromise;
}

/**
 * Namespace-correct Temporal UI deep link for a workflow (uses the configured
 * namespace, not a hardcoded `default`).
 */
export function temporalUiUrl(workflowId: string): string {
  const base = process.env.TEMPORAL_UI_BASE_URL ?? "http://localhost:8080";
  return `${base}/namespaces/${namespace()}/workflows/${encodeURIComponent(workflowId)}`;
}
