import "server-only";

import { getTemporalClient, WORKFLOW } from "./temporal-client";

/**
 * Server-only facade over the WaveExecutionWorkflow, used by the wave server actions.
 * The UI is the Temporal client: it starts the workflow, sends its signals, and runs
 * its queries directly. Shapes below mirror the Java DTOs so the cross-language JSON
 * payloads line up.
 */

export interface WaveOrderLineInput {
  orderLineId: number;
  sku: string;
  quantity: number;
}

export interface WaveShipToInput {
  name?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
}

export interface WaveOrderInput {
  orderId: number;
  externalOrderId: string;
  orderLines: WaveOrderLineInput[];
  shipTo?: WaveShipToInput | null;
}

/** Mirrors Java `WaveExecutionRequest`. */
export interface WaveExecutionInput {
  tenantId: string;
  waveId: number;
  facilityId: number;
  waveNumber: string;
  orders: WaveOrderInput[];
  fulfillmentMode: "STANDARD" | "EXPRESS" | "AUTO_SHIP";
  defaultCarrier?: string;
  defaultServiceLevel?: string;
}

/**
 * Start the WaveExecutionWorkflow on the WMS task queue. Idempotent: a repeated
 * release for the same wave reuses the running execution (USE_EXISTING) rather than
 * failing or starting a duplicate. Returns the workflow id.
 */
export async function startWaveExecution(input: WaveExecutionInput): Promise<string> {
  const client = await getTemporalClient();
  const workflowId = WORKFLOW.waveExecution.workflowId(input.waveId);

  await client.workflow.start(WORKFLOW.waveExecution.type, {
    taskQueue: WORKFLOW.waveExecution.taskQueue,
    workflowId,
    args: [input],
    workflowIdConflictPolicy: "USE_EXISTING",
  });

  return workflowId;
}

async function waveHandle(waveId: number) {
  const client = await getTemporalClient();
  return client.workflow.getHandle(WORKFLOW.waveExecution.workflowId(waveId));
}

/**
 * Send a signal to a wave's workflow. Signal names and positional args must match the
 * Java @SignalMethod exactly (e.g. `rateSelected(shipmentId, carrier, serviceLevel)`).
 */
export async function signalWave(
  waveId: number,
  signalName: string,
  ...args: unknown[]
): Promise<void> {
  const handle = await waveHandle(waveId);
  await handle.signal(signalName, ...args);
}

/**
 * Query a wave's workflow. Query names and args match the Java @QueryMethod. Throws if
 * the workflow does not exist / is not running — callers decide how to fall back.
 */
export async function queryWave<T>(
  waveId: number,
  queryName: string,
  ...args: unknown[]
): Promise<T> {
  const handle = await waveHandle(waveId);
  return handle.query<T, unknown[]>(queryName, ...args);
}

/**
 * Run an Update against a wave's workflow (request-response). The update name and
 * positional args match the Java @UpdateMethod; the promise resolves with the handler's
 * return value once the update completes, or rejects if the validator rejects it.
 */
export async function updateWave<T>(
  waveId: number,
  updateName: string,
  ...args: [unknown, ...unknown[]]
): Promise<T> {
  const handle = await waveHandle(waveId);
  return handle.executeUpdate(updateName, { args }) as Promise<T>;
}
