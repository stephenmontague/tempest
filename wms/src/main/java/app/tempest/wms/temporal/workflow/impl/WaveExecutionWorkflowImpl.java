package app.tempest.wms.temporal.workflow.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.tempest.common.dto.CarrierRateDTO;
import app.tempest.common.dto.FetchedRatesDTO;
import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.PickItemDTO;
import app.tempest.common.dto.ShipToDTO;
import app.tempest.common.dto.ShipmentStateDTO;
import app.tempest.common.dto.WaveOrderDTO;
import app.tempest.common.dto.WaveStatusDTO;
import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.requests.AllocateInventoryRequest;
import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.requests.ConsumeInventoryRequest;
import app.tempest.common.dto.requests.CreatePickWaveRequest;
import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.requests.ReleaseInventoryRequest;
import app.tempest.common.dto.requests.SelectRateRequest;
import app.tempest.common.dto.requests.WaveExecutionRequest;
import app.tempest.common.dto.results.AllocateInventoryResult;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import app.tempest.common.dto.results.OrderShipmentResult;
import app.tempest.common.dto.results.WaveExecutionResult;
import app.tempest.common.dto.requests.UpdateWaveStatusRequest;
import app.tempest.common.temporal.TaskQueues;
import app.tempest.common.dto.results.FetchRatesResult;
import app.tempest.common.temporal.activities.ims.ImsActivities;
import app.tempest.common.temporal.activities.oms.OmsActivities;
import app.tempest.common.temporal.activities.sms.FetchFedExRatesActivity;
import app.tempest.common.temporal.activities.sms.FetchUPSRatesActivity;
import app.tempest.common.temporal.activities.sms.FetchUSPSRatesActivity;
import app.tempest.common.temporal.activities.sms.SmsActivities;
import app.tempest.common.temporal.activities.wms.WmsActivities;
import app.tempest.wms.temporal.activities.UpdateWaveStatusActivity;
import app.tempest.wms.temporal.workflow.WaveExecutionWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

/**
 * Implementation of WaveExecutionWorkflow with HITL shipment handling.
 * 
 * Orchestrates batch fulfillment of orders in a wave:
 * 1. Allocate inventory for all orders
 * 2. Mark orders as reserved
 * 3. Create pick tasks for the wave
 * 4. Wait for all picks to complete (signal)
 * 5. Consume inventory
 * 6. Wait for all packs to complete (signal)
 * 7. Create shipments (auto)
 * 8. HITL: For each shipment:
 * - Optional: Select rate (signal)
 * - Print label (signal triggers activity)
 * - Confirm shipped (signal)
 * 9. Mark orders as shipped when all shipments confirmed
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

     // Per-shipment tracking for HITL
     private final Map<Long, ShipmentStateDTO> shipmentStates = new HashMap<>();
     private final Map<Long, Long> orderToShipmentMap = new HashMap<>(); // orderId -> shipmentId
     private final Set<Long> shipmentsToGenerateLabel = new HashSet<>();
     private final Set<Long> shipmentsToConfirm = new HashSet<>();

     // Rate fetching state
     private final Map<Long, FetchedRatesDTO> fetchedRatesMap = new HashMap<>();
     private final Set<Long> shipmentsToFetchRates = new HashSet<>();

     // Counters
     private int ordersAllocated = 0;
     private int ordersPicked = 0;
     private int ordersPacked = 0;
     private int ordersShipped = 0;
     private int ordersFailed = 0;

     // Request data (for queries and activities)
     private Long waveId;
     private String tenantId;
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
     private final WmsActivities wmsActivities = Workflow.newActivityStub(
               WmsActivities.class,
               defaultActivityOptions);

     // WMS Activity to update wave status in DB
     private final UpdateWaveStatusActivity updateWaveStatusActivity = Workflow.newActivityStub(
               UpdateWaveStatusActivity.class,
               defaultActivityOptions);

     // SMS Activities (on sms-tasks queue)
     private final SmsActivities smsActivities = Workflow.newActivityStub(
               SmsActivities.class,
               ActivityOptions.newBuilder(defaultActivityOptions)
                         .setTaskQueue(TaskQueues.SMS)
                         .build());

     // Per-carrier rate fetching activities with higher retry count for FedEx demo
     private final ActivityOptions rateActivityOptions = ActivityOptions.newBuilder()
               .setStartToCloseTimeout(Duration.ofSeconds(30))
               .setRetryOptions(RetryOptions.newBuilder()
                         .setMaximumAttempts(10) // Allow up to 10 attempts for FedEx failures
                         .setInitialInterval(Duration.ofSeconds(1))
                         .setBackoffCoefficient(1.5)
                         .build())
               .setTaskQueue(TaskQueues.SMS)
               .build();

     private final FetchUSPSRatesActivity uspsRatesActivity = Workflow.newActivityStub(
               FetchUSPSRatesActivity.class,
               rateActivityOptions);

     private final FetchUPSRatesActivity upsRatesActivity = Workflow.newActivityStub(
               FetchUPSRatesActivity.class,
               rateActivityOptions);

     private final FetchFedExRatesActivity fedexRatesActivity = Workflow.newActivityStub(
               FetchFedExRatesActivity.class,
               rateActivityOptions);

     @Override
     public WaveExecutionResult execute(WaveExecutionRequest request) {
          this.waveId = request.getWaveId();
          this.tenantId = request.getTenantId();
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
                    if (cancelled)
                         break;
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
                    if (failedOrderIds.contains(order.getOrderId()))
                         continue;

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
                    if (failedOrderIds.contains(order.getOrderId()))
                         continue;

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

                    wmsActivities.createPickWave(pickRequest);
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
                    if (failedOrderIds.contains(order.getOrderId()))
                         continue;

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

               // Step 7: Create shipments for all orders (auto after packs complete)
               currentStep = "CREATING_SHIPMENTS";
               status = "SHIPPING";

               for (WaveOrderDTO order : request.getOrders()) {
                    if (failedOrderIds.contains(order.getOrderId()))
                         continue;

                    try {
                         ShipmentStateDTO shipmentState = createShipmentForOrder(order, request.getFacilityId());
                         shipmentStates.put(shipmentState.getShipmentId(), shipmentState);
                         orderToShipmentMap.put(order.getOrderId(), shipmentState.getShipmentId());
                         orderStatuses.put(order.getOrderId(), "SHIPMENT_CREATED");
                         ordersPacked++;
                    } catch (Exception e) {
                         orderStatuses.put(order.getOrderId(), "SHIPMENT_FAILED");
                         failedOrderIds.add(order.getOrderId());
                         ordersFailed++;
                    }
               }

               // Step 8: HITL - Wait for all shipments to be confirmed
               currentStep = "WAITING_FOR_SHIPMENTS";
               blockingReason = "Waiting for shipments: print labels and confirm shipped";

               // Process label generation requests as they come in
               while (!allShipmentsConfirmed() && !cancelled) {
                    // Wait for either a rate fetch, label request, a confirmation, or cancellation
                    Workflow.await(() -> !shipmentsToFetchRates.isEmpty() ||
                              !shipmentsToGenerateLabel.isEmpty() ||
                              !shipmentsToConfirm.isEmpty() ||
                              allShipmentsConfirmed() ||
                              cancelled);

                    // Process pending rate fetches (parallel carrier calls)
                    for (Long shipmentId : new HashSet<>(shipmentsToFetchRates)) {
                         shipmentsToFetchRates.remove(shipmentId);
                         fetchRatesForShipment(shipmentId);
                    }

                    // Process pending label generations
                    for (Long shipmentId : new HashSet<>(shipmentsToGenerateLabel)) {
                         shipmentsToGenerateLabel.remove(shipmentId);
                         generateLabelForShipment(shipmentId);
                    }

                    // Process pending confirmations
                    for (Long shipmentId : new HashSet<>(shipmentsToConfirm)) {
                         shipmentsToConfirm.remove(shipmentId);
                         confirmShipment(shipmentId);
                    }
               }

               blockingReason = null;

               if (cancelled) {
                    return handleCancellation(request);
               }

               // Step 9: Update wave status in database
               currentStep = "UPDATING_WAVE_STATUS";

               UpdateWaveStatusRequest updateRequest = UpdateWaveStatusRequest.builder()
                         .tenantId(tenantId)
                         .waveId(waveId)
                         .status("COMPLETED")
                         .build();
               updateWaveStatusActivity.updateStatus(updateRequest);

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

               // Try to update wave status to FAILED
               try {
                    UpdateWaveStatusRequest updateRequest = UpdateWaveStatusRequest.builder()
                              .tenantId(tenantId)
                              .waveId(waveId)
                              .status("FAILED")
                              .build();
                    updateWaveStatusActivity.updateStatus(updateRequest);
               } catch (Exception ignored) {
                    // Best effort - don't fail the workflow if status update fails
               }

               throw e;
          }
     }

     private boolean allShipmentsConfirmed() {
          if (shipmentStates.isEmpty())
               return false;
          return shipmentStates.values().stream()
                    .allMatch(s -> "SHIPPED".equals(s.getStatus()));
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

     private ShipmentStateDTO createShipmentForOrder(WaveOrderDTO order, Long facilityId) {
          ShipToDTO shipTo = order.getShipTo();
          CreateShipmentRequest shipmentRequest = CreateShipmentRequest.builder()
                    .tenantId(tenantId)
                    .orderId(order.getOrderId())
                    .facilityId(facilityId)
                    .carrier("PENDING") // Default - user can select rate
                    .serviceLevel("STANDARD")
                    .shipTo(shipTo)
                    .build();

          CreateShipmentResult shipmentResult = smsActivities.createShipment(shipmentRequest);

          return ShipmentStateDTO.builder()
                    .shipmentId(shipmentResult.getShipmentId())
                    .orderId(order.getOrderId())
                    .status("CREATED")
                    .carrier("PENDING")
                    .serviceLevel("STANDARD")
                    .build();
     }

     /**
      * Fetch rates from all carriers in parallel.
      * USPS and UPS will succeed immediately.
      * FedEx will fail 4 times before succeeding on the 5th attempt.
      */
     private void fetchRatesForShipment(Long shipmentId) {
          ShipmentStateDTO shipment = shipmentStates.get(shipmentId);
          if (shipment == null)
               return;

          // Initialize the fetched rates state
          FetchedRatesDTO ratesState = FetchedRatesDTO.builder()
                    .shipmentId(shipmentId)
                    .status("FETCHING")
                    .uspsStatus("FETCHING")
                    .upsStatus("FETCHING")
                    .fedexStatus("FETCHING")
                    .rates(new ArrayList<>())
                    .build();
          fetchedRatesMap.put(shipmentId, ratesState);

          FetchRatesRequest request = FetchRatesRequest.builder()
                    .tenantId(tenantId)
                    .shipmentId(shipmentId)
                    .orderId(shipment.getOrderId())
                    .build();

          // Launch all three carrier rate fetches in parallel using Async.function
          Promise<FetchRatesResult> uspsPromise = Async.function(
                    uspsRatesActivity::fetchUSPSRates, request);
          Promise<FetchRatesResult> upsPromise = Async.function(
                    upsRatesActivity::fetchUPSRates, request);
          Promise<FetchRatesResult> fedexPromise = Async.function(
                    fedexRatesActivity::fetchFedExRates, request);

          // Wait for all to complete
          Promise.allOf(uspsPromise, upsPromise, fedexPromise).get();

          // Collect all rates
          List<CarrierRateDTO> allRates = new ArrayList<>();

          FetchRatesResult uspsResult = uspsPromise.get();
          allRates.addAll(uspsResult.getRates());
          ratesState.setUspsStatus("COMPLETED");

          FetchRatesResult upsResult = upsPromise.get();
          allRates.addAll(upsResult.getRates());
          ratesState.setUpsStatus("COMPLETED");

          FetchRatesResult fedexResult = fedexPromise.get();
          allRates.addAll(fedexResult.getRates());
          ratesState.setFedexStatus("COMPLETED");

          // Update final state
          ratesState.setRates(allRates);
          ratesState.setStatus("COMPLETED");
     }

     private void generateLabelForShipment(Long shipmentId) {
          ShipmentStateDTO shipment = shipmentStates.get(shipmentId);
          if (shipment == null)
               return;

          GenerateShippingLabelRequest labelRequest = GenerateShippingLabelRequest.builder()
                    .tenantId(tenantId)
                    .shipmentId(shipmentId)
                    .orderId(shipment.getOrderId())
                    .carrier(shipment.getCarrier())
                    .serviceLevel(shipment.getServiceLevel())
                    .build();

          GenerateShippingLabelResult labelResult = smsActivities.generateLabel(labelRequest);

          // Update shipment state
          shipment.setTrackingNumber(labelResult.getTrackingNumber());
          shipment.setLabelUrl(labelResult.getLabelUrl());
          shipment.setStatus("LABEL_GENERATED");
     }

     private void confirmShipment(Long shipmentId) {
          ShipmentStateDTO shipment = shipmentStates.get(shipmentId);
          if (shipment == null)
               return;

          ConfirmShipmentRequest confirmRequest = ConfirmShipmentRequest.builder()
                    .tenantId(tenantId)
                    .shipmentId(shipmentId)
                    .orderId(shipment.getOrderId())
                    .shippedAt(Instant.now())
                    .build();
          smsActivities.confirmShipment(confirmRequest);

          // Update shipment state
          shipment.setStatus("SHIPPED");

          // Mark order as shipped in OMS immediately (don't wait for all shipments)
          MarkOrderShippedRequest shippedRequest = MarkOrderShippedRequest.builder()
                    .orderId(shipment.getOrderId())
                    .shipmentId(shipment.getShipmentId())
                    .trackingNumber(shipment.getTrackingNumber())
                    .carrier(shipment.getCarrier())
                    .build();
          omsActivities.markOrderShipped(shippedRequest);

          // Update local tracking state
          orderShipments.add(OrderShipmentResult.builder()
                    .orderId(shipment.getOrderId())
                    .shipmentId(shipment.getShipmentId())
                    .trackingNumber(shipment.getTrackingNumber())
                    .status("SHIPPED")
                    .build());

          orderStatuses.put(shipment.getOrderId(), "SHIPPED");
          ordersShipped++;
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

          // Update wave status in database
          UpdateWaveStatusRequest updateRequest = UpdateWaveStatusRequest.builder()
                    .tenantId(tenantId)
                    .waveId(waveId)
                    .status("CANCELLED")
                    .build();
          updateWaveStatusActivity.updateStatus(updateRequest);

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

     // Signal handlers

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
     public void rateSelected(Long shipmentId, String carrier, String serviceLevel) {
          ShipmentStateDTO shipment = shipmentStates.get(shipmentId);
          if (shipment != null && "CREATED".equals(shipment.getStatus())) {
               // Call activity to update the shipment in DB
               SelectRateRequest selectRequest = SelectRateRequest.builder()
                         .tenantId(tenantId)
                         .shipmentId(shipmentId)
                         .carrier(carrier)
                         .serviceLevel(serviceLevel)
                         .build();
               smsActivities.selectRate(selectRequest);

               // Update local state
               shipment.setCarrier(carrier);
               shipment.setServiceLevel(serviceLevel);
               shipment.setStatus("RATE_SELECTED");
          }
     }

     @Override
     public void printLabel(Long shipmentId) {
          ShipmentStateDTO shipment = shipmentStates.get(shipmentId);
          if (shipment != null &&
                    ("CREATED".equals(shipment.getStatus()) || "RATE_SELECTED".equals(shipment.getStatus()))) {
               shipmentsToGenerateLabel.add(shipmentId);
          }
     }

     @Override
     public void shipmentConfirmed(Long shipmentId) {
          ShipmentStateDTO shipment = shipmentStates.get(shipmentId);
          if (shipment != null && "LABEL_GENERATED".equals(shipment.getStatus())) {
               shipmentsToConfirm.add(shipmentId);
          }
     }

     @Override
     public void fetchRates(Long shipmentId) {
          ShipmentStateDTO shipment = shipmentStates.get(shipmentId);
          if (shipment != null && "CREATED".equals(shipment.getStatus())) {
               shipmentsToFetchRates.add(shipmentId);
          }
     }

     // Query handlers

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

     @Override
     public Map<Long, ShipmentStateDTO> getShipmentStates() {
          return new HashMap<>(shipmentStates);
     }

     @Override
     public FetchedRatesDTO getFetchedRates(Long shipmentId) {
          FetchedRatesDTO rates = fetchedRatesMap.get(shipmentId);
          if (rates == null) {
               return FetchedRatesDTO.builder()
                         .shipmentId(shipmentId)
                         .status("PENDING")
                         .rates(List.of())
                         .build();
          }
          return rates;
     }
}
