package app.tempest.oms.temporal.workflow.impl;

import java.time.Duration;

import app.tempest.common.dto.requests.CreateOrderRequest;
import app.tempest.common.dto.requests.MarkOrderAwaitingWaveRequest;
import app.tempest.common.dto.requests.OrderIntakeWorkflowRequest;
import app.tempest.common.dto.requests.ValidateOrderRequest;
import app.tempest.common.dto.results.CreateOrderResult;
import app.tempest.common.dto.results.OrderIntakeWorkflowResult;
import app.tempest.oms.temporal.activities.CreateOrderActivity;
import app.tempest.oms.temporal.activities.MarkOrderAwaitingWaveActivity;
import app.tempest.oms.temporal.activities.ValidateOrderActivity;
import app.tempest.oms.temporal.workflow.OrderIntakeWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

public class OrderIntakeWorkflowImpl implements OrderIntakeWorkflow {

     // Workflow state for query
     private String status = "RECEIVED";
     private Long orderId;

     // Activity stubs - configured with retry policies
     private final ValidateOrderActivity validateOrderActivity = Workflow.newActivityStub(
               ValidateOrderActivity.class,
               ActivityOptions.newBuilder()
                         .setStartToCloseTimeout(Duration.ofSeconds(30))
                         .setRetryOptions(RetryOptions.newBuilder()
                                   .setMaximumAttempts(3)
                                   .build())
                         .build());

     private final CreateOrderActivity createOrderActivity = Workflow.newActivityStub(
               CreateOrderActivity.class,
               ActivityOptions.newBuilder()
                         .setStartToCloseTimeout(Duration.ofSeconds(30))
                         .setRetryOptions(RetryOptions.newBuilder()
                                   .setMaximumAttempts(5)
                                   .setInitialInterval(Duration.ofSeconds(1))
                                   .setBackoffCoefficient(2.0)
                                   .build())
                         .build());

     private final MarkOrderAwaitingWaveActivity markOrderAwaitingWaveActivity = Workflow.newActivityStub(
               MarkOrderAwaitingWaveActivity.class,
               ActivityOptions.newBuilder()
                         .setStartToCloseTimeout(Duration.ofSeconds(30))
                         .setRetryOptions(RetryOptions.newBuilder()
                                   .setMaximumAttempts(5)
                                   .setInitialInterval(Duration.ofSeconds(1))
                                   .setBackoffCoefficient(2.0)
                                   .build())
                         .build());

     @Override
     public OrderIntakeWorkflowResult execute(OrderIntakeWorkflowRequest request) {
          // Step 1: Validate Order
          status = "VALIDATING";
          ValidateOrderRequest validateRequest = ValidateOrderRequest.builder()
                    .requestId(request.getRequestId())
                    .externalOrderId(request.getExternalOrderId())
                    .channel(request.getChannel())
                    .orderLines(request.getOrderLines())
                    .shipTo(request.getShipTo())
                    .build();

          validateOrderActivity.validate(validateRequest);
          status = "VALIDATED";

          // Step 2: Create Order
          status = "CREATING";
          CreateOrderRequest createRequest = CreateOrderRequest.builder()
                    .requestId(request.getRequestId())
                    .externalOrderId(request.getExternalOrderId())
                    .channel(request.getChannel())
                    .priority(request.getPriority())
                    .orderLines(request.getOrderLines())
                    .shipTo(request.getShipTo())
                    .build();

          CreateOrderResult createResult = createOrderActivity.createOrder(createRequest);
          orderId = createResult.getOrderId();
          status = "CREATED";

          // Step 3: Mark Order as Awaiting Wave
          // Order is now ready for wave planning - fulfillment will be triggered
          // when a warehouse manager creates and releases a wave containing this order
          status = "MARKING_AWAITING_WAVE";
          MarkOrderAwaitingWaveRequest awaitingWaveRequest = MarkOrderAwaitingWaveRequest.builder()
                    .orderId(orderId)
                    .facilityId(request.getFacilityId())
                    .build();

          markOrderAwaitingWaveActivity.markAwaitingWave(awaitingWaveRequest);
          status = "AWAITING_WAVE";

          // Workflow completes here - no child workflow started
          // Fulfillment will be triggered by WaveExecutionWorkflow when wave is released

          return OrderIntakeWorkflowResult.builder()
                    .orderId(orderId)
                    .status(status)
                    .build();
     }

     @Override
     public String getStatus() {
          return status;
     }
}
