package app.tempest.oms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.ShipToDTO;
import app.tempest.common.dto.requests.OrderIntakeWorkflowRequest;
import app.tempest.common.dto.results.OrderIntakeWorkflowResult;
import app.tempest.oms.dto.CreateOrderCommand;
import app.tempest.oms.entity.Order;
import app.tempest.oms.entity.OrderLine;
import app.tempest.oms.repository.OrderLineRepository;
import app.tempest.oms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for Order operations.
 * Contains business logic for order management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

     private final OrderRepository orderRepository;
     private final OrderLineRepository orderLineRepository;
     private final OrderWorkflowService orderWorkflowService;

     @Transactional(readOnly = true)
     public List<Order> getOrders(String tenantId, String status, String sku) {
          log.debug("Fetching orders for tenant: {}, status filter: {}, sku filter: {}", tenantId, status, sku);

          // SKU filter takes precedence - find orders containing this SKU
          if (sku != null && !sku.isEmpty()) {
               return orderRepository.findByTenantIdAndOrderLinesSku(tenantId, sku);
          }

          if (status != null && !status.isEmpty()) {
               return orderRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status);
          }
          return orderRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
     }

     @Transactional(readOnly = true)
     public Optional<Order> getOrder(Long id, String tenantId) {
          log.debug("Fetching order {} for tenant: {}", id, tenantId);
          return orderRepository.findByIdAndTenantId(id, tenantId);
     }

     @Transactional(readOnly = true)
     public Optional<List<OrderLine>> getOrderLines(Long orderId, String tenantId) {
          log.debug("Fetching order lines for order {} tenant: {}", orderId, tenantId);

          // Verify the order belongs to the tenant
          if (orderRepository.findByIdAndTenantId(orderId, tenantId).isEmpty()) {
               return Optional.empty();
          }

          return Optional.of(orderLineRepository.findByOrderIdAndTenantId(orderId, tenantId));
     }

     /**
      * Create an order by executing the OrderIntakeWorkflow synchronously.
      * This waits for the workflow to complete and returns the created order ID.
      *
      * @param command the order creation command
      * @return the result containing the order ID and status
      */
     public OrderIntakeWorkflowResult createOrder(CreateOrderCommand command) {
          log.info("Starting order intake workflow for externalOrderId {} for tenant: {} by user: {}",
                    command.externalOrderId(), command.tenantId(), command.userId());

          String requestId = UUID.randomUUID().toString();

          List<OrderLineDTO> lineDTOs = command.lines() != null
                    ? command.lines().stream()
                              .map(l -> OrderLineDTO.builder()
                                        .sku(l.sku())
                                        .quantity(l.quantity() != null ? l.quantity() : 0)
                                        .unitPrice(l.unitPrice())
                                        .build())
                              .toList()
                    : List.of();

          // Build shipping info from command
          ShipToDTO shipTo = ShipToDTO.builder()
                    .name(command.customerName())
                    .addressLine1(command.shippingAddressLine1())
                    .addressLine2(command.shippingAddressLine2())
                    .city(command.shippingCity())
                    .state(command.shippingState())
                    .postalCode(command.shippingPostalCode())
                    .country(command.shippingCountry())
                    .build();

          OrderIntakeWorkflowRequest workflowRequest = OrderIntakeWorkflowRequest.builder()
                    .requestId(requestId)
                    .tenantId(command.tenantId())
                    .userId(command.userId())
                    .externalOrderId(command.externalOrderId())
                    .customerEmail(command.customerEmail())
                    .orderLines(lineDTOs)
                    .shipTo(shipTo)
                    .build();

          // Execute workflow synchronously - waits for completion
          OrderIntakeWorkflowResult result = orderWorkflowService.startOrderIntake(workflowRequest);

          log.info("Order intake workflow completed - orderId: {}, status: {}, externalOrderId: {}, tenant: {}",
                    result.getOrderId(), result.getStatus(), command.externalOrderId(), command.tenantId());

          return result;
     }

     @Transactional(readOnly = true)
     public Map<String, Long> getOrderCounts(String tenantId) {
          log.debug("Fetching order counts for tenant: {}", tenantId);

          Map<String, Long> counts = new HashMap<>();
          counts.put("CREATED", orderRepository.countByTenantIdAndStatus(tenantId, "CREATED"));
          counts.put("VALIDATED", orderRepository.countByTenantIdAndStatus(tenantId, "VALIDATED"));
          counts.put("RESERVED", orderRepository.countByTenantIdAndStatus(tenantId, "RESERVED"));
          counts.put("AWAITING_WAVE", orderRepository.countByTenantIdAndStatus(tenantId, "AWAITING_WAVE"));
          counts.put("IN_WAVE", orderRepository.countByTenantIdAndStatus(tenantId, "IN_WAVE"));
          counts.put("PICKING", orderRepository.countByTenantIdAndStatus(tenantId, "PICKING"));
          counts.put("PACKING", orderRepository.countByTenantIdAndStatus(tenantId, "PACKING"));
          counts.put("SHIPPED", orderRepository.countByTenantIdAndStatus(tenantId, "SHIPPED"));
          counts.put("CANCELLED", orderRepository.countByTenantIdAndStatus(tenantId, "CANCELLED"));

          return counts;
     }

     @Transactional
     public boolean cancelOrder(Long orderId, String tenantId, String reason) {
          log.info("Cancelling order {} for tenant: {}, reason: {}", orderId, tenantId, reason);

          return orderRepository.findByIdAndTenantId(orderId, tenantId)
                    .map(order -> {
                         if (order.getWorkflowId() != null) {
                              try {
                                   orderWorkflowService.signalCancelOrder(order.getWorkflowId(), reason);
                              } catch (Exception e) {
                                   log.warn("Failed to signal workflow cancellation: {}", e.getMessage());
                              }
                         }
                         order.setStatus("CANCELLED");
                         orderRepository.save(order);
                         return true;
                    })
                    .orElse(false);
     }

     @Transactional(readOnly = true)
     public Optional<WorkflowStatus> getOrderWorkflowStatus(Long orderId, String tenantId) {
          log.debug("Fetching workflow status for order {} tenant: {}", orderId, tenantId);

          return orderRepository.findByIdAndTenantId(orderId, tenantId)
                    .flatMap(order -> {
                         if (order.getWorkflowId() == null) {
                              return Optional.empty();
                         }
                         return getWorkflowStatus(order.getWorkflowId());
                    });
     }

     private Optional<WorkflowStatus> getWorkflowStatus(String workflowId) {
          try {
               // Try OrderFulfillmentWorkflow first
               String status = orderWorkflowService.getFulfillmentStatus(workflowId);
               String currentStep = orderWorkflowService.getCurrentStep(workflowId);
               String blockingReason = orderWorkflowService.getBlockingReason(workflowId);
               return Optional.of(new WorkflowStatus(status, currentStep, blockingReason));
          } catch (Exception e) {
               // Fall back to OrderIntakeWorkflow
               try {
                    String status = orderWorkflowService.getOrderIntakeStatus(workflowId);
                    return Optional.of(new WorkflowStatus(status, null, null));
               } catch (Exception e2) {
                    log.warn("Could not get workflow status for {}: {}", workflowId, e2.getMessage());
                    return Optional.empty();
               }
          }
     }

     public record WorkflowStatus(String status, String currentStep, String blockingReason) {
     }

     /**
      * Update the status of an order.
      * Used by Temporal activities to transition order status.
      *
      * @param orderId the order ID
      * @param newStatus the new status
      */
     @Transactional
     public void updateOrderStatus(Long orderId, String newStatus) {
          log.info("Updating order {} status to {}", orderId, newStatus);

          Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

          String previousStatus = order.getStatus();
          order.setStatus(newStatus);
          orderRepository.save(order);

          log.info("Order {} status updated: {} -> {}", orderId, previousStatus, newStatus);
     }
}
