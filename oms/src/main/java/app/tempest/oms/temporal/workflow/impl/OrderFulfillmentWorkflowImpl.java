package app.tempest.oms.temporal.workflow.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.PickItemDTO;
import app.tempest.common.dto.ShipToDTO;
import app.tempest.common.dto.requests.AllocateInventoryRequest;
import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.requests.ConsumeInventoryRequest;
import app.tempest.common.dto.requests.CreatePickWaveRequest;
import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.requests.OrderFulfillmentWorkflowRequest;
import app.tempest.common.dto.requests.ReleaseInventoryRequest;
import app.tempest.common.dto.results.AllocateInventoryResult;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import app.tempest.common.dto.results.OrderFulfillmentWorkflowResult;
import app.tempest.common.temporal.TaskQueues;
import app.tempest.oms.temporal.activities.MarkOrderReservedActivity;
import app.tempest.oms.temporal.activities.MarkOrderShippedActivity;
import app.tempest.oms.temporal.activities.remote.ImsActivities;
import app.tempest.oms.temporal.activities.remote.SmsActivities;
import app.tempest.oms.temporal.activities.remote.WmsActivities;
import app.tempest.oms.temporal.workflow.OrderFulfillmentWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

/**
 * Legacy/Express single-order fulfillment workflow implementation.
 * For standard fulfillment, use WaveExecutionWorkflow instead.
 */
public class OrderFulfillmentWorkflowImpl implements OrderFulfillmentWorkflow {

     // Workflow state
     private String status = "STARTED";
     private String currentStep = "INITIALIZING";
     private String blockingReason = null;
     private boolean pickCompleted = false;
     private boolean packCompleted = false;
     private boolean cancelled = false;
     private String cancellationReason = null;

     // Result data
     private Long shipmentId;
     private String trackingNumber;

     // Default activity options with retry
     private final ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
               .setStartToCloseTimeout(Duration.ofSeconds(30))
               .setRetryOptions(RetryOptions.newBuilder()
                         .setMaximumAttempts(5)
                         .setInitialInterval(Duration.ofSeconds(1))
                         .setBackoffCoefficient(2.0)
                         .build())
               .build();

     // IMS Activities (on ims-tasks queue)
     private final ImsActivities imsActivities = Workflow.newActivityStub(
               ImsActivities.class,
               ActivityOptions.newBuilder(defaultActivityOptions)
                         .setTaskQueue(TaskQueues.IMS)
                         .build());

     // OMS Activities (on oms-tasks queue - same as workflow)
     private final MarkOrderReservedActivity markOrderReservedActivity = Workflow.newActivityStub(
               MarkOrderReservedActivity.class,
               defaultActivityOptions);

     private final MarkOrderShippedActivity markOrderShippedActivity = Workflow.newActivityStub(
               MarkOrderShippedActivity.class,
               defaultActivityOptions);

     // WMS Activities (on wms-tasks queue)
     private final WmsActivities wmsActivities = Workflow.newActivityStub(
               WmsActivities.class,
               ActivityOptions.newBuilder(defaultActivityOptions)
                         .setTaskQueue(TaskQueues.WMS)
                         .build());

     // SMS Activities (on sms-tasks queue)
     private final SmsActivities smsActivities = Workflow.newActivityStub(
               SmsActivities.class,
               ActivityOptions.newBuilder(defaultActivityOptions)
                         .setTaskQueue(TaskQueues.SMS)
                         .build());

     @Override
     public OrderFulfillmentWorkflowResult execute(OrderFulfillmentWorkflowRequest request) {
          Long orderId = request.getOrderId();

          try {
               // Step 1: Allocate Inventory
               currentStep = "ALLOCATING_INVENTORY";
               status = "ALLOCATING";

               String reservationId = null;
               for (OrderLineDTO line : request.getOrderLines()) {
                    AllocateInventoryRequest allocateRequest = AllocateInventoryRequest.builder()
                              .orderId(orderId)
                              .sku(line.getSku())
                              .quantity(line.getQuantity())
                              .build();

                    AllocateInventoryResult allocateResult = imsActivities.allocate(allocateRequest);
                    reservationId = allocateResult.getReservationId();
               }

               // Check for cancellation
               if (cancelled) {
                    return handleCancellation(orderId, reservationId);
               }

               // Step 2: Mark Order Reserved
               currentStep = "MARKING_RESERVED";
               status = "RESERVED";

               MarkOrderReservedRequest reservedRequest = MarkOrderReservedRequest.builder()
                         .orderId(orderId)
                         .reservationId(reservationId)
                         .build();
               markOrderReservedActivity.markReserved(reservedRequest);

               // Step 3: Create Pick Wave
               currentStep = "CREATING_PICK_WAVE";
               status = "PICKING";

               List<PickItemDTO> pickItems = request.getOrderLines().stream()
                         .map(line -> PickItemDTO.builder()
                                   .sku(line.getSku())
                                   .quantity(line.getQuantity())
                                   .build())
                         .collect(Collectors.toList());

               CreatePickWaveRequest pickWaveRequest = CreatePickWaveRequest.builder()
                         .orderId(orderId)
                         .facilityId(request.getFacilityId() != null ? request.getFacilityId() : 1L)
                         .strategy("SINGLE_ORDER")
                         .items(pickItems)
                         .build();

               // Execute pick wave creation (result used for logging/debugging if needed)
               wmsActivities.createPickWave(pickWaveRequest);

               // Step 4: Wait for Pick Completion (signal)
               currentStep = "WAITING_FOR_PICK";
               blockingReason = "Waiting for pick completion signal";

               Workflow.await(() -> pickCompleted || cancelled);
               blockingReason = null;

               if (cancelled) {
                    return handleCancellation(orderId, reservationId);
               }

               // Step 5: Consume Inventory
               currentStep = "CONSUMING_INVENTORY";

               for (OrderLineDTO line : request.getOrderLines()) {
                    ConsumeInventoryRequest consumeRequest = ConsumeInventoryRequest.builder()
                              .orderId(String.valueOf(orderId))
                              .reservationId(reservationId)
                              .sku(line.getSku())
                              .quantity(line.getQuantity())
                              .build();
                    imsActivities.consumeInventory(consumeRequest);
               }

               // Step 6: Wait for Pack Completion (signal)
               currentStep = "WAITING_FOR_PACK";
               status = "PACKING";
               blockingReason = "Waiting for pack completion signal";

               Workflow.await(() -> packCompleted || cancelled);
               blockingReason = null;

               if (cancelled) {
                    return handleCancellation(orderId, reservationId);
               }

               // Step 7: Create Shipment
               currentStep = "CREATING_SHIPMENT";
               status = "SHIPPING";

               ShipToDTO shipTo = request.getShipTo();

               CreateShipmentRequest shipmentRequest = CreateShipmentRequest.builder()
                         .orderId(orderId)
                         .facilityId(request.getFacilityId() != null ? request.getFacilityId() : 1L)
                         .carrier("STUB_CARRIER")
                         .serviceLevel("GROUND")
                         .shipTo(shipTo)
                         .build();

               CreateShipmentResult shipmentResult = smsActivities.createShipment(shipmentRequest);
               shipmentId = shipmentResult.getShipmentId();

               // Step 8: Generate Shipping Label
               currentStep = "GENERATING_LABEL";

               GenerateShippingLabelRequest labelRequest = GenerateShippingLabelRequest.builder()
                         .shipmentId(shipmentId)
                         .orderId(orderId)
                         .carrier("STUB_CARRIER")
                         .serviceLevel("GROUND")
                         .build();

               GenerateShippingLabelResult labelResult = smsActivities.generateLabel(labelRequest);
               trackingNumber = labelResult.getTrackingNumber();

               // Step 9: Confirm Shipment
               currentStep = "CONFIRMING_SHIPMENT";

               ConfirmShipmentRequest confirmRequest = ConfirmShipmentRequest.builder()
                         .shipmentId(shipmentId)
                         .orderId(orderId)
                         .shippedAt(Instant.now())
                         .build();
               smsActivities.confirmShipment(confirmRequest);

               // Step 10: Mark Order Shipped
               currentStep = "MARKING_SHIPPED";
               status = "SHIPPED";

               MarkOrderShippedRequest shippedRequest = MarkOrderShippedRequest.builder()
                         .orderId(orderId)
                         .shipmentId(shipmentId)
                         .trackingNumber(trackingNumber)
                         .carrier("STUB_CARRIER")
                         .build();
               markOrderShippedActivity.markShipped(shippedRequest);

               currentStep = "COMPLETED";

               return OrderFulfillmentWorkflowResult.builder()
                         .orderId(orderId)
                         .shipmentId(shipmentId)
                         .trackingNumber(trackingNumber)
                         .finalStatus("SHIPPED")
                         .build();

          } catch (Exception e) {
               status = "FAILED";
               currentStep = "FAILED";
               blockingReason = e.getMessage();
               throw e;
          }
     }

     private OrderFulfillmentWorkflowResult handleCancellation(Long orderId, String reservationId) {
          status = "CANCELLED";
          currentStep = "RELEASING_INVENTORY";

          // Release inventory if allocated
          if (reservationId != null) {
               ReleaseInventoryRequest releaseRequest = ReleaseInventoryRequest.builder()
                         .orderId(String.valueOf(orderId))
                         .reservationId(reservationId)
                         .reason(cancellationReason)
                         .build();
               imsActivities.releaseInventory(releaseRequest);
          }

          currentStep = "CANCELLED";

          return OrderFulfillmentWorkflowResult.builder()
                    .orderId(orderId)
                    .finalStatus("CANCELLED")
                    .build();
     }

     @Override
     public void pickCompleted() {
          this.pickCompleted = true;
     }

     @Override
     public void packCompleted() {
          this.packCompleted = true;
     }

     @Override
     public void cancelOrder(String reason) {
          this.cancelled = true;
          this.cancellationReason = reason;
     }

     @Override
     public String getFulfillmentStatus() {
          return status;
     }

     @Override
     public String getCurrentStep() {
          return currentStep;
     }

     @Override
     public String getBlockingReason() {
          return blockingReason;
     }
}
