package app.tempest.oms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import app.tempest.common.dto.results.CreateOrderResult;
import app.tempest.oms.dto.CreateOrderCommand;
import app.tempest.oms.dto.OrderLineCommand;
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
      * Create an order.
      *
      * Order intake is a single-service CRUD operation (validate -> persist ->
      * mark AWAITING_WAVE), so it runs as one local database transaction rather than
      * a Temporal workflow: it spans no other service, waits on nothing, and needs no
      * compensation. Fulfillment orchestration is driven later by the WMS wave workflow
      * when a warehouse manager creates and releases a wave containing this order.
      *
      * @param command the order creation command
      * @return the created (or pre-existing) order's id and status
      */
     @Transactional
     public CreateOrderResult createOrder(CreateOrderCommand command) {
          log.info("Creating order - externalOrderId: {}, tenant: {}, user: {}",
                    command.externalOrderId(), command.tenantId(), command.userId());

          // Minimal, demo-friendly validation: an order just needs at least one line item.
          // (Shipping address is optional here.)
          if (command.lines() == null || command.lines().isEmpty()) {
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must have at least one line item");
          }

          // Idempotency: a repeat submission of the same (tenant, externalOrderId) returns the
          // existing order instead of creating a duplicate.
          Optional<Order> existing = orderRepository.findByTenantIdAndExternalOrderId(
                    command.tenantId(), command.externalOrderId());
          if (existing.isPresent()) {
               Order order = existing.get();
               log.info("Order already exists - orderId: {}, externalOrderId: {}",
                         order.getId(), command.externalOrderId());
               return CreateOrderResult.builder()
                         .orderId(order.getId())
                         .status(order.getStatus())
                         .alreadyExisted(true)
                         .build();
          }

          Order order = Order.builder()
                    .externalOrderId(command.externalOrderId())
                    .customerEmail(command.customerEmail())
                    .status("AWAITING_WAVE")
                    .build();
          order.setTenantId(command.tenantId());
          order.setCreatedByUserId(command.userId());
          order.setUpdatedByUserId(command.userId());
          order.setCustomerName(command.customerName());
          order.setShippingAddressLine1(command.shippingAddressLine1());
          order.setShippingAddressLine2(command.shippingAddressLine2());
          order.setShippingCity(command.shippingCity());
          order.setShippingState(command.shippingState());
          order.setShippingPostalCode(command.shippingPostalCode());
          order.setShippingCountry(command.shippingCountry());
          order = orderRepository.save(order);

          for (OrderLineCommand lineCmd : command.lines()) {
               OrderLine line = OrderLine.builder()
                         .order(order)
                         .sku(lineCmd.sku())
                         .quantity(lineCmd.quantity() != null ? lineCmd.quantity() : 0)
                         .unitPrice(lineCmd.unitPrice())
                         .build();
               line.setTenantId(command.tenantId());
               line.setCreatedByUserId(command.userId());
               line.setUpdatedByUserId(command.userId());
               orderLineRepository.save(line);
          }

          log.info("Order created - orderId: {}, status: {}, lines: {}",
                    order.getId(), order.getStatus(), command.lines().size());

          return CreateOrderResult.builder()
                    .orderId(order.getId())
                    .status(order.getStatus())
                    .alreadyExisted(false)
                    .build();
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
                         // Note: Order cancellation during wave execution should be done via WMS wave
                         // cancellation
                         // This only updates the order status in OMS
                         order.setStatus("CANCELLED");
                         orderRepository.save(order);
                         return true;
                    })
                    .orElse(false);
     }

     /**
      * Return the current lifecycle status of an order, read straight from the order row.
      * (Order intake is no longer a workflow, so there is no intake workflow to query;
      * fulfillment progress is queried from the WMS wave workflow instead.)
      */
     @Transactional(readOnly = true)
     public Optional<WorkflowStatus> getOrderWorkflowStatus(Long orderId, String tenantId) {
          log.debug("Fetching status for order {} tenant: {}", orderId, tenantId);

          return orderRepository.findByIdAndTenantId(orderId, tenantId)
                    .map(order -> new WorkflowStatus(order.getStatus(), null, null));
     }

     public record WorkflowStatus(String status, String currentStep, String blockingReason) {
     }

     /**
      * Update the status of an order.
      * Used by Temporal activities to transition order status.
      *
      * @param orderId   the order ID
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
