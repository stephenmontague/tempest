package app.tempest.oms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.MarkOrderAwaitingWaveRequest;
import app.tempest.common.dto.results.MarkOrderAwaitingWaveResult;
import app.tempest.oms.temporal.activities.MarkOrderAwaitingWaveActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MarkOrderAwaitingWaveActivityImpl implements MarkOrderAwaitingWaveActivity {

     @Override
     public MarkOrderAwaitingWaveResult markAwaitingWave(MarkOrderAwaitingWaveRequest request) {
          log.info("Marking order as AWAITING_WAVE - orderId: {}, facilityId: {}",
                    request.getOrderId(), request.getFacilityId());

          // Stub implementation for now
          // TODO: Implement actual status transition logic
          // 1. Find order by ID
          // 2. Validate current status allows transition to AWAITING_WAVE
          // 3. If already AWAITING_WAVE, return success (idempotent)
          // 4. Update status to AWAITING_WAVE
          // 5. Set facilityId on order (for wave planning)
          // 6. Emit OrderEvent (ORDER_AWAITING_WAVE)

          log.info("Order marked as AWAITING_WAVE - orderId: {}", request.getOrderId());

          return MarkOrderAwaitingWaveResult.builder()
                    .success(true)
                    .previousStatus("CREATED")
                    .currentStatus("AWAITING_WAVE")
                    .build();
     }
}
