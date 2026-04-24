package app.tempest.oms.temporal;

import java.time.Duration;

import org.springframework.stereotype.Service;

import app.tempest.common.dto.requests.OrderIntakeWorkflowRequest;
import app.tempest.common.dto.results.OrderIntakeWorkflowResult;
import app.tempest.common.temporal.TaskQueues;
import app.tempest.common.temporal.workflows.OrderIntakeWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for starting and interacting with order-related Temporal workflows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWorkflowClient {

     private final WorkflowClient workflowClient;

     public OrderIntakeWorkflowResult startOrderIntake(OrderIntakeWorkflowRequest request) {
          String workflowId = "order-intake-" + request.getRequestId();

          log.info("Starting OrderIntakeWorkflow - workflowId: {}, requestId: {}",
                    workflowId, request.getRequestId());

          OrderIntakeWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderIntakeWorkflow.class,
                    WorkflowOptions.newBuilder()
                              .setTaskQueue(TaskQueues.WORKFLOWS)
                              .setWorkflowId(workflowId)
                              .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                              .build());

          // Execute workflow synchronously and return result
          return workflow.execute(request);
     }

     public String startOrderIntakeAsync(OrderIntakeWorkflowRequest request) {
          String workflowId = "order-intake-" + request.getRequestId();

          log.info("Starting OrderIntakeWorkflow async - workflowId: {}, requestId: {}",
                    workflowId, request.getRequestId());

          OrderIntakeWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderIntakeWorkflow.class,
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
      * Query the status of an OrderIntakeWorkflow.
      * 
      * @param workflowId the workflow ID
      * @return the current status
      */
     public String getOrderIntakeStatus(String workflowId) {
          OrderIntakeWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderIntakeWorkflow.class, workflowId);
          return workflow.getStatus();
     }
}
