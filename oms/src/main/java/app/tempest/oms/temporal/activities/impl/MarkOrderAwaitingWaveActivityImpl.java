package app.tempest.oms.temporal.activities.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.requests.MarkOrderAwaitingWaveRequest;
import app.tempest.common.dto.results.MarkOrderAwaitingWaveResult;
import app.tempest.oms.entity.Order;
import app.tempest.oms.repository.OrderRepository;
import app.tempest.oms.temporal.activities.MarkOrderAwaitingWaveActivity;
import io.temporal.failure.ApplicationFailure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkOrderAwaitingWaveActivityImpl implements MarkOrderAwaitingWaveActivity {

     private final OrderRepository orderRepository;

     @Override
     @Transactional
     public MarkOrderAwaitingWaveResult markAwaitingWave(MarkOrderAwaitingWaveRequest request) {
          log.info("Marking order as AWAITING_WAVE - orderId: {}, facilityId: {}",
                    request.getOrderId(), request.getFacilityId());

          Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> ApplicationFailure.newNonRetryableFailure(
                              "Order not found: " + request.getOrderId(),
                              "ORDER_NOT_FOUND"));

          String previousStatus = order.getStatus();

          // Idempotency: if already AWAITING_WAVE, return success
          if ("AWAITING_WAVE".equals(previousStatus)) {
               log.info("Order already AWAITING_WAVE - orderId: {}", request.getOrderId());
               return MarkOrderAwaitingWaveResult.builder()
                         .success(true)
                         .previousStatus(previousStatus)
                         .currentStatus("AWAITING_WAVE")
                         .build();
          }

          // Validate state transition
          if (!"CREATED".equals(previousStatus)) {
               throw ApplicationFailure.newNonRetryableFailure(
                         "Cannot transition to AWAITING_WAVE from status: " + previousStatus,
                         "INVALID_STATE_TRANSITION");
          }

          // Update status
          order.setStatus("AWAITING_WAVE");
          orderRepository.save(order);

          log.info("Order marked as AWAITING_WAVE - orderId: {}, previousStatus: {}",
                    request.getOrderId(), previousStatus);

          return MarkOrderAwaitingWaveResult.builder()
                    .success(true)
                    .previousStatus(previousStatus)
                    .currentStatus("AWAITING_WAVE")
                    .build();
     }
}
