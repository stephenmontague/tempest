package app.tempest.oms.temporal;

import java.time.Duration;

import org.springframework.stereotype.Service;

import app.tempest.common.dto.requests.RandomDAGWorkflowRequest;
import app.tempest.common.dto.results.RandomDAGWorkflowResult;
import app.tempest.common.temporal.TaskQueues;
import app.tempest.common.temporal.workflows.RandomDAGWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for starting and interacting with RandomDAGWorkflow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RandomDAGWorkflowClient {

        private final WorkflowClient workflowClient;

        /**
         * Start a RandomDAGWorkflow asynchronously.
         * 
         * @param request The workflow request
         * @return The workflow ID
         */
        public String startRandomDAGWorkflow(RandomDAGWorkflowRequest request) {
                String workflowId = "random-dag-" + request.getRequestId();

                log.info("Starting RandomDAGWorkflow - workflowId: {}, requestId: {}, steps: {}",
                                workflowId, request.getRequestId(), request.getSteps());

                RandomDAGWorkflow workflow = workflowClient.newWorkflowStub(
                                RandomDAGWorkflow.class,
                                WorkflowOptions.newBuilder()
                                                .setTaskQueue(TaskQueues.WORKFLOWS)
                                                .setWorkflowId(workflowId)
                                                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                                                .build());

                // Start workflow asynchronously
                WorkflowClient.start(workflow::execute, request);

                return workflowId;
        }

        /**
         * Start a RandomDAGWorkflow synchronously (waits for completion).
         * 
         * @param request The workflow request
         * @return The workflow result
         */
        public RandomDAGWorkflowResult startRandomDAGWorkflowSync(RandomDAGWorkflowRequest request) {
                String workflowId = "random-dag-" + request.getRequestId();

                log.info("Starting RandomDAGWorkflow (sync) - workflowId: {}, requestId: {}, steps: {}",
                                workflowId, request.getRequestId(), request.getSteps());

                RandomDAGWorkflow workflow = workflowClient.newWorkflowStub(
                                RandomDAGWorkflow.class,
                                WorkflowOptions.newBuilder()
                                                .setTaskQueue(TaskQueues.WORKFLOWS)
                                                .setWorkflowId(workflowId)
                                                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                                                .build());

                // Execute workflow synchronously and return result
                return workflow.execute(request);
        }

        /**
         * Query the status of a RandomDAGWorkflow.
         * 
         * @param workflowId The workflow ID
         * @return The current status
         */
        public String getStatus(String workflowId) {
                RandomDAGWorkflow workflow = workflowClient.newWorkflowStub(
                                RandomDAGWorkflow.class, workflowId);
                return workflow.getStatus();
        }

        /**
         * Query the current step of a RandomDAGWorkflow.
         * 
         * @param workflowId The workflow ID
         * @return The current step
         */
        public String getCurrentStep(String workflowId) {
                RandomDAGWorkflow workflow = workflowClient.newWorkflowStub(
                                RandomDAGWorkflow.class, workflowId);
                return workflow.getCurrentStep();
        }
}
