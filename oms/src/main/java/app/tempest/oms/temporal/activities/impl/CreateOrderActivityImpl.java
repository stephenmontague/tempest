package app.tempest.oms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.CreateOrderRequest;
import app.tempest.common.dto.results.CreateOrderResult;
import app.tempest.oms.temporal.activities.CreateOrderActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateOrderActivityImpl implements CreateOrderActivity {

     // TODO: Inject OrderRepository when ready
     // private final OrderRepository orderRepository;
     //
     // public CreateOrderActivityImpl(OrderRepository orderRepository) {
     //     this.orderRepository = orderRepository;
     // }

     @Override
     public CreateOrderResult createOrder(CreateOrderRequest request) {
          log.info("Creating order - requestId: {}, externalOrderId: {}",
                    request.getRequestId(), request.getExternalOrderId());

          // Stub implementation for now
          // TODO: Implement actual order creation logic
          // 1. Check if order with requestId already exists (idempotency)
          // 2. If exists, return existing order
          // 3. Create Order entity with status CREATED
          // 4. Create OrderLine entities
          // 5. Emit OrderEvent (ORDER_CREATED)
          // 6. Return order details

          // Simulate order ID generation
          Long orderId = System.currentTimeMillis();

          log.info("Order created - orderId: {}, requestId: {}", orderId, request.getRequestId());

          return CreateOrderResult.builder()
                    .orderId(orderId)
                    .status("CREATED")
                    .alreadyExisted(false)
                    .build();
     }
}
