package app.tempest.oms.temporal.workflow;

import app.tempest.common.dto.requests.OrderFulfillmentWorkflowRequest;
import app.tempest.common.dto.results.OrderFulfillmentWorkflowResult;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Legacy/Express single-order fulfillment workflow.
 * For standard fulfillment, use WaveExecutionWorkflow instead.
 */
@WorkflowInterface
public interface OrderFulfillmentWorkflow {

     @WorkflowMethod
     OrderFulfillmentWorkflowResult execute(OrderFulfillmentWorkflowRequest request);

     @SignalMethod
     void pickCompleted();

     @SignalMethod
     void packCompleted();

     @SignalMethod
     void cancelOrder(String reason);

     @QueryMethod
     String getFulfillmentStatus();

     @QueryMethod
     String getCurrentStep();

     @QueryMethod
     String getBlockingReason();
}
