package app.tempest.oms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.results.MarkOrderReservedResult;
import app.tempest.oms.temporal.activities.MarkOrderReservedActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MarkOrderReservedActivityImpl implements MarkOrderReservedActivity {

     @Override
     public MarkOrderReservedResult markReserved(MarkOrderReservedRequest request) {
          log.info("Marking order as RESERVED - orderId: {}", request.getOrderId());

          // Stub implementation for now
          // TODO: Implement actual status transition logic
          // 1. Find order by ID
          // 2. Validate current status allows transition to RESERVED
          // 3. If already RESERVED, return success (idempotent)
          // 4. Update status to RESERVED
          // 5. Emit OrderEvent (ORDER_RESERVED)

          log.info("Order marked as RESERVED - orderId: {}", request.getOrderId());

          return MarkOrderReservedResult.builder()
                    .success(true)
                    .previousStatus("AWAITING_WAVE")
                    .currentStatus("RESERVED")
                    .build();
     }
}
