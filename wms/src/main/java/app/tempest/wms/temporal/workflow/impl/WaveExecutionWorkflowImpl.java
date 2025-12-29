package app.tempest.wms.temporal.workflow.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.PickItemDTO;
import app.tempest.common.dto.ShipToDTO;
import app.tempest.common.dto.WaveOrderDTO;
import app.tempest.common.dto.WaveStatusDTO;
import app.tempest.common.dto.requests.AllocateInventoryRequest;
import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.requests.ConsumeInventoryRequest;
import app.tempest.common.dto.requests.CreatePickWaveRequest;
import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.requests.ReleaseInventoryRequest;
import app.tempest.common.dto.requests.WaveExecutionRequest;
import app.tempest.common.dto.results.AllocateInventoryResult;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import app.tempest.common.dto.results.OrderShipmentResult;
import app.tempest.common.dto.results.WaveExecutionResult;
import app.tempest.common.temporal.TaskQueues;
import app.tempest.wms.temporal.activities.CreatePickWaveActivity;
import app.tempest.wms.temporal.activities.remote.ImsActivities;
import app.tempest.wms.temporal.activities.remote.OmsActivities;
import app.tempest.wms.temporal.activities.remote.SmsActivities;
import app.tempest.wms.temporal.workflow.WaveExecutionWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

/**
 * Implementation of WaveExecutionWorkflow.
 * 
 * Orchestrates batch fulfillment of orders in a wave:
 * 1. Allocate inventory for all orders
 * 2. Mark orders as reserved
 * 3. Create pick tasks for the wave
 * 4. Wait for all picks to complete (signal)
 * 5. Consume inventory
 * 6. Wait for all packs to complete (signal)
 * 7. Create shipments and labels
 * 8. Mark orders as shipped
 */
public class WaveExecutionWorkflowImpl implements WaveExecutionWorkflow {

     // Workflow state
     private String status = "STARTED";
     private String currentStep = "INITIALIZING";
     private String blockingReason = null;
     private boolean allPicksCompleted = false;
     private boolean allPacksCompleted = false;
     private boolean cancelled = false;
     private String cancellationReason = null;

     // Per-order tracking
     private final Set<Long> ordersPickCompleted = new HashSet<>();
     private final Set<Long> ordersPackCompleted = new HashSet<>();
     private final Map<Long, String> orderStatuses = new HashMap<>();
     private final Map<Long, String> orderReservationIds = new HashMap<>();
     private final List<Long> failedOrderIds = new ArrayList<>();
     private final List<OrderShipmentResult> orderShipments = new ArrayList<>();

     // Counters
     private int ordersAllocated = 0;
     private int ordersPicked = 0;
     private int ordersPacked = 0;
     private int ordersShipped = 0;
     private int ordersFailed = 0;

     // Request data (for queries)
     private Long waveId;
     private int totalOrders = 0;

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

     // OMS Activities (on oms-tasks queue)
     private final OmsActivities omsActivities = Workflow.newActivityStub(
               OmsActivities.class,
               ActivityOptions.newBuilder(defaultActivityOptions)
                         .setTaskQueue(TaskQueues.OMS)
                         .build());

     // WMS Activities (local - same task queue as workflow)
     private final CreatePickWaveActivity createPickWaveActivity = Workflow.newActivityStub(
               CreatePickWaveActivity.class,
               defaultActivityOptions);

     // SMS Activities (on sms-tasks queue)
     private final SmsActivities smsActivities = Workflow.newActivityStub(
               SmsActivities.class,
               ActivityOptions.newBuilder(defaultActivityOptions)
                         .setTaskQueue(TaskQueues.SMS)
                         .build());

     @Override
     public WaveExecutionResult execute(WaveExecutionRequest request) {
          this.waveId = request.getWaveId();
          this.totalOrders = request.getOrders().size();

          // Initialize order statuses
          for (WaveOrderDTO order : request.getOrders()) {
               orderStatuses.put(order.getOrderId(), "PENDING");
          }

          try {
               // Step 1: Allocate Inventory for all orders
               currentStep = "ALLOCATING_INVENTORY";
               status = "ALLOCATING";

               for (WaveOrderDTO order : request.getOrders()) {
                    if (cancelled) break;

                    try {
                         allocateInventoryForOrder(order);
                         orderStatuses.put(order.getOrderId(), "ALLOCATED");
                         ordersAllocated++;
                    } catch (Exception e) {
                         orderStatuses.put(order.getOrderId(), "ALLOCATION_FAILED");
                         failedOrderIds.add(order.getOrderId());
                         ordersFailed++;
                    }
               }

               if (cancelled) {
                    return handleCancellation(request);
               }

               // Step 2: Mark orders as reserved
               currentStep = "MARKING_RESERVED";
               status = "RESERVED";

               for (WaveOrderDTO order : request.getOrders()) {
                    if (failedOrderIds.contains(order.getOrderId())) continue;

                    MarkOrderReservedRequest reservedRequest = MarkOrderReservedRequest.builder()
                              .orderId(order.getOrderId())
                              .reservationId(orderReservationIds.get(order.getOrderId()))
                              .build();
                    omsActivities.markOrderReserved(reservedRequest);
                    orderStatuses.put(order.getOrderId(), "RESERVED");
               }

               // Step 3: Create pick tasks for the wave
               currentStep = "CREATING_PICK_TASKS";
               status = "PICKING";

               for (WaveOrderDTO order : request.getOrders()) {
                    if (failedOrderIds.contains(order.getOrderId())) continue;

                    List<PickItemDTO> pickItems = order.getOrderLines().stream()
                              .map(line -> PickItemDTO.builder()
                                        .sku(line.getSku())
                                        .quantity(line.getQuantity())
                                        .build())
                              .toList();

                    CreatePickWaveRequest pickRequest = CreatePickWaveRequest.builder()
                              .orderId(order.getOrderId())
                              .facilityId(request.getFacilityId())
                              .strategy("WAVE")
                              .items(pickItems)
                              .build();

                    createPickWaveActivity.createPickWave(pickRequest);
                    orderStatuses.put(order.getOrderId(), "PICKING");
               }

               // Step 4: Wait for all picks to complete
               currentStep = "WAITING_FOR_PICKS";
               blockingReason = "Waiting for all picks to complete";

               Workflow.await(() -> allPicksCompleted || cancelled);
               blockingReason = null;

               if (cancelled) {
                    return handleCancellation(request);
               }

               // Step 5: Consume inventory for all orders
               currentStep = "CONSUMING_INVENTORY";

               for (WaveOrderDTO order : request.getOrders()) {
                    if (failedOrderIds.contains(order.getOrderId())) continue;

                    consumeInventoryForOrder(order);
                    orderStatuses.put(order.getOrderId(), "PICKED");
                    ordersPicked++;
               }

               // Step 6: Wait for all packs to complete
               currentStep = "WAITING_FOR_PACKS";
               status = "PACKING";
               blockingReason = "Waiting for all packs to complete";

               Workflow.await(() -> allPacksCompleted || cancelled);
               blockingReason = null;

               if (cancelled) {
                    return handleCancellation(request);
               }

               // Step 7: Create shipments and labels for all orders
               currentStep = "CREATING_SHIPMENTS";
               status = "SHIPPING";

               for (WaveOrderDTO order : request.getOrders()) {
                    if (failedOrderIds.contains(order.getOrderId())) continue;

                    try {
                         OrderShipmentResult shipmentResult = createShipmentForOrder(order, request.getFacilityId());
                         orderShipments.add(shipmentResult);
                         orderStatuses.put(order.getOrderId(), "SHIPPED");
                         ordersShipped++;
                         ordersPacked++;
                    } catch (Exception e) {
                         orderStatuses.put(order.getOrderId(), "SHIPPING_FAILED");
                         failedOrderIds.add(order.getOrderId());
                         ordersFailed++;
                         orderShipments.add(OrderShipmentResult.builder()
                                   .orderId(order.getOrderId())
                                   .status("FAILED")
                                   .failureReason(e.getMessage())
                                   .build());
                    }
               }

               // Step 8: Mark orders as shipped in OMS
               currentStep = "MARKING_SHIPPED";

               for (OrderShipmentResult shipment : orderShipments) {
                    if ("SHIPPED".equals(shipment.getStatus())) {
                         MarkOrderShippedRequest shippedRequest = MarkOrderShippedRequest.builder()
                                   .orderId(shipment.getOrderId())
                                   .shipmentId(shipment.getShipmentId())
                                   .trackingNumber(shipment.getTrackingNumber())
                                   .carrier("STUB_CARRIER")
                                   .build();
                         omsActivities.markOrderShipped(shippedRequest);
                    }
               }

               currentStep = "COMPLETED";
               status = "COMPLETED";

               return WaveExecutionResult.builder()
                         .waveId(waveId)
                         .finalStatus("COMPLETED")
                         .totalOrders(totalOrders)
                         .successfulOrders(ordersShipped)
                         .failedOrders(ordersFailed)
                         .orderShipments(orderShipments)
                         .build();

          } catch (Exception e) {
               status = "FAILED";
               currentStep = "FAILED";
               blockingReason = e.getMessage();
               throw e;
          }
     }

     private void allocateInventoryForOrder(WaveOrderDTO order) {
          String reservationId = null;
          for (OrderLineDTO line : order.getOrderLines()) {
               AllocateInventoryRequest allocateRequest = AllocateInventoryRequest.builder()
                         .orderId(order.getOrderId())
                         .sku(line.getSku())
                         .quantity(line.getQuantity())
                         .build();

               AllocateInventoryResult result = imsActivities.allocate(allocateRequest);
               reservationId = result.getReservationId();
          }
          orderReservationIds.put(order.getOrderId(), reservationId);
     }

     private void consumeInventoryForOrder(WaveOrderDTO order) {
          String reservationId = orderReservationIds.get(order.getOrderId());
          for (OrderLineDTO line : order.getOrderLines()) {
               ConsumeInventoryRequest consumeRequest = ConsumeInventoryRequest.builder()
                         .orderId(String.valueOf(order.getOrderId()))
                         .reservationId(reservationId)
                         .sku(line.getSku())
                         .quantity(line.getQuantity())
                         .build();
               imsActivities.consumeInventory(consumeRequest);
          }
     }

     private OrderShipmentResult createShipmentForOrder(WaveOrderDTO order, Long facilityId) {
          // Create shipment
          ShipToDTO shipTo = order.getShipTo();
          CreateShipmentRequest shipmentRequest = CreateShipmentRequest.builder()
                    .orderId(order.getOrderId())
                    .facilityId(facilityId)
                    .carrier("STUB_CARRIER")
                    .serviceLevel("GROUND")
                    .shipTo(shipTo)
                    .build();

          CreateShipmentResult shipmentResult = smsActivities.createShipment(shipmentRequest);
          Long shipmentId = shipmentResult.getShipmentId();

          // Generate label
          GenerateShippingLabelRequest labelRequest = GenerateShippingLabelRequest.builder()
                    .shipmentId(shipmentId)
                    .orderId(order.getOrderId())
                    .carrier("STUB_CARRIER")
                    .serviceLevel("GROUND")
                    .build();

          GenerateShippingLabelResult labelResult = smsActivities.generateLabel(labelRequest);
          String trackingNumber = labelResult.getTrackingNumber();

          // Confirm shipment
          ConfirmShipmentRequest confirmRequest = ConfirmShipmentRequest.builder()
                    .shipmentId(shipmentId)
                    .orderId(order.getOrderId())
                    .shippedAt(Instant.now())
                    .build();
          smsActivities.confirmShipment(confirmRequest);

          return OrderShipmentResult.builder()
                    .orderId(order.getOrderId())
                    .shipmentId(shipmentId)
                    .trackingNumber(trackingNumber)
                    .status("SHIPPED")
                    .build();
     }

     private WaveExecutionResult handleCancellation(WaveExecutionRequest request) {
          status = "CANCELLED";
          currentStep = "RELEASING_INVENTORY";

          // Release inventory for all allocated orders
          for (WaveOrderDTO order : request.getOrders()) {
               String reservationId = orderReservationIds.get(order.getOrderId());
               if (reservationId != null) {
                    ReleaseInventoryRequest releaseRequest = ReleaseInventoryRequest.builder()
                              .orderId(String.valueOf(order.getOrderId()))
                              .reservationId(reservationId)
                              .reason(cancellationReason)
                              .build();
                    imsActivities.releaseInventory(releaseRequest);
               }
               orderStatuses.put(order.getOrderId(), "CANCELLED");
          }

          currentStep = "CANCELLED";

          return WaveExecutionResult.builder()
                    .waveId(waveId)
                    .finalStatus("CANCELLED")
                    .totalOrders(totalOrders)
                    .successfulOrders(0)
                    .failedOrders(totalOrders)
                    .orderShipments(List.of())
                    .build();
     }

     @Override
     public void allPicksCompleted() {
          this.allPicksCompleted = true;
     }

     @Override
     public void allPacksCompleted() {
          this.allPacksCompleted = true;
     }

     @Override
     public void cancelWave(String reason) {
          this.cancelled = true;
          this.cancellationReason = reason;
     }

     @Override
     public void orderPickCompleted(Long orderId) {
          ordersPickCompleted.add(orderId);
     }

     @Override
     public void orderPackCompleted(Long orderId) {
          ordersPackCompleted.add(orderId);
     }

     @Override
     public WaveStatusDTO getWaveStatus() {
          return WaveStatusDTO.builder()
                    .waveId(waveId)
                    .status(status)
                    .currentStep(currentStep)
                    .blockingReason(blockingReason)
                    .totalOrders(totalOrders)
                    .ordersAllocated(ordersAllocated)
                    .ordersPicked(ordersPicked)
                    .ordersPacked(ordersPacked)
                    .ordersShipped(ordersShipped)
                    .ordersFailed(ordersFailed)
                    .orderStatuses(new HashMap<>(orderStatuses))
                    .failedOrderIds(new ArrayList<>(failedOrderIds))
                    .build();
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
