package app.tempest.oms.temporal;

import java.time.Duration;

import org.springframework.stereotype.Service;

import app.tempest.common.dto.requests.OrderIntakeWorkflowRequest;
import app.tempest.common.dto.results.OrderIntakeWorkflowResult;
import app.tempest.common.temporal.TaskQueues;
import app.tempest.oms.temporal.workflow.OrderFulfillmentWorkflow;
import app.tempest.oms.temporal.workflow.OrderIntakeWorkflow;
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
                              .setTaskQueue(TaskQueues.OMS)
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
                              .setTaskQueue(TaskQueues.OMS)
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

     /**
      * Query the status of an OrderFulfillmentWorkflow.
      * 
      * @param workflowId the workflow ID
      * @return the current fulfillment status
      */
     public String getFulfillmentStatus(String workflowId) {
          OrderFulfillmentWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderFulfillmentWorkflow.class, workflowId);
          return workflow.getFulfillmentStatus();
     }

     /**
      * Query the current step of an OrderFulfillmentWorkflow.
      * 
      * @param workflowId the workflow ID
      * @return the current step
      */
     public String getCurrentStep(String workflowId) {
          OrderFulfillmentWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderFulfillmentWorkflow.class, workflowId);
          return workflow.getCurrentStep();
     }

     /**
      * Query the blocking reason of an OrderFulfillmentWorkflow.
      * 
      * @param workflowId the workflow ID
      * @return the blocking reason (null if not blocked)
      */
     public String getBlockingReason(String workflowId) {
          OrderFulfillmentWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderFulfillmentWorkflow.class, workflowId);
          return workflow.getBlockingReason();
     }

     /**
      * Signal pick completion for an OrderFulfillmentWorkflow.
      * 
      * @param workflowId the workflow ID
      */
     public void signalPickCompleted(String workflowId) {
          log.info("Signaling pick completed - workflowId: {}", workflowId);
          OrderFulfillmentWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderFulfillmentWorkflow.class, workflowId);
          workflow.pickCompleted();
     }

     /**
      * Signal pack completion for an OrderFulfillmentWorkflow.
      * 
      * @param workflowId the workflow ID
      */
     public void signalPackCompleted(String workflowId) {
          log.info("Signaling pack completed - workflowId: {}", workflowId);
          OrderFulfillmentWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderFulfillmentWorkflow.class, workflowId);
          workflow.packCompleted();
     }

     /**
      * Signal order cancellation for an OrderFulfillmentWorkflow.
      * 
      * @param workflowId the workflow ID
      * @param reason     the cancellation reason
      */
     public void signalCancelOrder(String workflowId, String reason) {
          log.info("Signaling order cancellation - workflowId: {}, reason: {}", workflowId, reason);
          OrderFulfillmentWorkflow workflow = workflowClient.newWorkflowStub(
                    OrderFulfillmentWorkflow.class, workflowId);
          workflow.cancelOrder(reason);
     }
}
