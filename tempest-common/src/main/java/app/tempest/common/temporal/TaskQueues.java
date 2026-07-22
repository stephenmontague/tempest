package app.tempest.common.temporal;

/**
 * Temporal task queue names shared across all services.
 * Each service's worker polls its designated queue.
 * Workflows use these constants when scheduling remote activities.
 */
public final class TaskQueues {

    private TaskQueues() {
        // Constants class - prevent instantiation
    }

    /** Order Management System - workflows and order activities */
    public static final String OMS = "oms-tasks";

    /** Inventory Management System - inventory activities */
    public static final String IMS = "ims-tasks";

    /** Warehouse Management System - picking/packing activities */
    public static final String WMS = "wms-tasks";

    /** Shipping Management System - shipment activities */
    public static final String SMS = "sms-tasks";
}

