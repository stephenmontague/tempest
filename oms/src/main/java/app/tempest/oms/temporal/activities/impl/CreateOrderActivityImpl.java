package app.tempest.oms.temporal.activities.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.requests.CreateOrderRequest;
import app.tempest.common.dto.results.CreateOrderResult;
import app.tempest.oms.entity.Order;
import app.tempest.oms.entity.OrderLine;
import app.tempest.oms.repository.OrderLineRepository;
import app.tempest.oms.repository.OrderRepository;
import app.tempest.oms.temporal.activities.CreateOrderActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderActivityImpl implements CreateOrderActivity {

     private final OrderRepository orderRepository;
     private final OrderLineRepository orderLineRepository;

     @Override
     @Transactional
     public CreateOrderResult createOrder(CreateOrderRequest request) {
          log.info("Creating order - requestId: {}, externalOrderId: {}, tenantId: {}",
                    request.getRequestId(), request.getExternalOrderId(), request.getTenantId());

          // Idempotency check: if order with this externalOrderId already exists for tenant, return it
          Optional<Order> existingOrder = orderRepository.findByTenantIdAndExternalOrderId(
                    request.getTenantId(), request.getExternalOrderId());

          if (existingOrder.isPresent()) {
               Order order = existingOrder.get();
               log.info("Order already exists - orderId: {}, requestId: {}", order.getId(), request.getRequestId());
               return CreateOrderResult.builder()
                         .orderId(order.getId())
                         .status(order.getStatus())
                         .alreadyExisted(true)
                         .build();
          }

          // Create new order
          Order order = Order.builder()
                    .externalOrderId(request.getExternalOrderId())
                    .customerEmail(request.getCustomerEmail())
                    .status("CREATED")
                    .build();
          order.setTenantId(request.getTenantId());
          order.setCreatedByUserId(request.getUserId());
          order.setUpdatedByUserId(request.getUserId());

          // Set shipping info if available
          if (request.getShipTo() != null) {
               order.setCustomerName(request.getShipTo().getName());
               order.setShippingAddressLine1(request.getShipTo().getAddressLine1());
               order.setShippingAddressLine2(request.getShipTo().getAddressLine2());
               order.setShippingCity(request.getShipTo().getCity());
               order.setShippingState(request.getShipTo().getState());
               order.setShippingPostalCode(request.getShipTo().getPostalCode());
               order.setShippingCountry(request.getShipTo().getCountry());
          }

          order = orderRepository.save(order);
          log.info("Order saved - orderId: {}, externalOrderId: {}", order.getId(), order.getExternalOrderId());

          // Create order lines
          if (request.getOrderLines() != null) {
               for (var lineDto : request.getOrderLines()) {
                    OrderLine line = OrderLine.builder()
                              .order(order)
                              .sku(lineDto.getSku())
                              .quantity(lineDto.getQuantity())
                              .unitPrice(lineDto.getUnitPrice())
                              .build();
                    line.setTenantId(request.getTenantId());
                    line.setCreatedByUserId(request.getUserId());
                    line.setUpdatedByUserId(request.getUserId());
                    orderLineRepository.save(line);
               }
               log.info("Created {} order lines for orderId: {}", request.getOrderLines().size(), order.getId());
          }

          return CreateOrderResult.builder()
                    .orderId(order.getId())
                    .status(order.getStatus())
                    .alreadyExisted(false)
                    .build();
     }
}
