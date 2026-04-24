package app.tempest.common.temporal.workflows;

import app.tempest.common.dto.requests.RandomDAGWorkflowRequest;
import app.tempest.common.dto.results.RandomDAGWorkflowResult;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow for demonstrating dynamic DAG execution.
 *
 * This workflow accepts an arbitrary ordering of steps (ALLOCATE, EMAIL,
 * PRINT_LABEL, SHIP)
 * and executes them in that order. This demonstrates Temporal's ability to
 * handle
 * dynamic workflow patterns deterministically.
 */
@WorkflowInterface
public interface RandomDAGWorkflow {

    /**
     * Execute the workflow with the provided step order.
     *
     * @param request The workflow request containing the ordered list of steps
     * @return The result of workflow execution
     */
    @WorkflowMethod
    RandomDAGWorkflowResult execute(RandomDAGWorkflowRequest request);

    /**
     * Query the current status of the workflow.
     *
     * @return The current status
     */
    @QueryMethod
    String getStatus();

    /**
     * Query the current step being executed.
     *
     * @return The current step name, or null if not started
     */
    @QueryMethod
    String getCurrentStep();
}
