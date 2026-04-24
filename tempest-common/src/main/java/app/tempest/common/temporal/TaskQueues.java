package app.tempest.common.temporal;

/**
 * Temporal task queue names shared across all services and workers.
 *
 * Two task queues separate workflow execution from activity execution:
 * - WORKFLOWS: Polled by the standalone workflow worker
 * - ACTIVITIES: Polled by each service's activity worker
 *
 * This separation allows workflows and activities to scale independently.
 */
public final class TaskQueues {

    private TaskQueues() {
        // Constants class - prevent instantiation
    }

    /** Polled by the standalone workflow worker - handles all workflow execution */
    public static final String WORKFLOWS = "tempest-workflows";

    /** Polled by each service's activity worker - handles all activity execution */
    public static final String ACTIVITIES = "tempest-activities";
}

