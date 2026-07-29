package app.tempest.oms.temporal.workflow.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import app.tempest.common.dto.requests.RandomDAGWorkflowRequest;
import app.tempest.common.dto.results.RandomDAGWorkflowResult;
import app.tempest.oms.temporal.activities.RandomDAGActivities;
import app.tempest.oms.temporal.workflow.RandomDAGWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

/**
 * Implementation of RandomDAGWorkflow.
 * 
 * This workflow demonstrates Temporal's ability to execute activities in an
 * arbitrary order
 * specified at runtime. The workflow iterates through the provided steps and
 * executes
 * the corresponding activities deterministically.
 */
public class RandomDAGWorkflowImpl implements RandomDAGWorkflow {

        // Workflow state for queries
        private String status = "INITIALIZING";
        private String currentStep = null;
        private final List<String> completedSteps = new ArrayList<>();
        private String demoId;

        // Activity stub with retry configuration
        private final RandomDAGActivities activities = Workflow.newActivityStub(
                        RandomDAGActivities.class,
                        ActivityOptions.newBuilder()
                                        .setStartToCloseTimeout(Duration.ofSeconds(30))
                                        .setRetryOptions(RetryOptions.newBuilder()
                                                        .setMaximumAttempts(3)
                                                        .setInitialInterval(Duration.ofSeconds(1))
                                                        .setBackoffCoefficient(2.0)
                                                        .build())
                                        .build());

        @Override
        public RandomDAGWorkflowResult execute(RandomDAGWorkflowRequest request) {
                // Generate a unique demo ID for this execution.
                // Use Workflow.randomUUID() (not UUID.randomUUID()) so the value is
                // deterministic across replays.
                demoId = "demo-" + Workflow.randomUUID().toString().substring(0, 8);

                status = "RUNNING";

                Workflow.getLogger(RandomDAGWorkflowImpl.class)
                                .info("Starting RandomDAGWorkflow - demoId: {}, steps: {}", demoId, request.getSteps());

                // Iterate through the provided steps and execute them in order
                for (String step : request.getSteps()) {
                        currentStep = step;

                        Workflow.getLogger(RandomDAGWorkflowImpl.class)
                                        .info("Executing step: {} - demoId: {}", step, demoId);

                        // Execute the corresponding activity based on the step name
                        switch (step) {
                                case "ALLOCATE" -> activities.allocate(demoId);
                                case "EMAIL" -> activities.sendEmail(demoId);
                                case "PRINT_LABEL" -> activities.printLabel(demoId);
                                case "SHIP" -> activities.ship(demoId);
                                default -> Workflow.getLogger(RandomDAGWorkflowImpl.class)
                                                .warn("Unknown step: {} - skipping", step);
                        }

                        completedSteps.add(step);
                }

                currentStep = null;
                status = "COMPLETED";

                Workflow.getLogger(RandomDAGWorkflowImpl.class)
                                .info("RandomDAGWorkflow completed - demoId: {}, completedSteps: {}", demoId,
                                                completedSteps);

                return RandomDAGWorkflowResult.builder()
                                .demoId(demoId)
                                .status(status)
                                .completedSteps(new ArrayList<>(completedSteps))
                                .build();
        }

        @Override
        public String getStatus() {
                return status;
        }

        @Override
        public String getCurrentStep() {
                return currentStep;
        }
}
