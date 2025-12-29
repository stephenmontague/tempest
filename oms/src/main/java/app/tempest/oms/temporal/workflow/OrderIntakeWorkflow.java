package app.tempest.oms.temporal.workflow;

import app.tempest.common.dto.requests.OrderIntakeWorkflowRequest;
import app.tempest.common.dto.results.OrderIntakeWorkflowResult;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface OrderIntakeWorkflow {

     @WorkflowMethod
     OrderIntakeWorkflowResult execute(OrderIntakeWorkflowRequest request);

     @QueryMethod
     String getStatus();
}
