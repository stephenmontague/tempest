package app.tempest.common.temporal;

/**
 * Temporal task queue names shared across all services and workers.
 *
 * WORKFLOWS: Polled by the standalone workflow worker.
 * Per-service queues: Each service's activity worker polls its own queue
 * so that activity tasks route to the worker that has those activities registered.
 */
public final class TaskQueues {

    private TaskQueues() {
        // Constants class - prevent instantiation
    }

    /** Polled by the standalone workflow worker - handles all workflow execution */
    public static final String WORKFLOWS = "tempest-workflows";

    /** Order Management System - order activities */
    public static final String OMS = "oms-activities";

    /** Inventory Management System - inventory activities */
    public static final String IMS = "ims-activities";

    /** Warehouse Management System - picking/packing activities */
    public static final String WMS = "wms-activities";

    /** Shipping Management System - shipment activities */
    public static final String SMS = "sms-activities";
}

