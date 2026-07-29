package app.tempest.oms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.results.MarkOrderShippedResult;
import app.tempest.oms.temporal.activities.MarkOrderShippedActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MarkOrderShippedActivityImpl implements MarkOrderShippedActivity {

     @Override
     public MarkOrderShippedResult markShipped(MarkOrderShippedRequest request) {
          log.info("Marking order as SHIPPED - orderId: {}, shipmentId: {}, trackingNumber: {}",
                    request.getOrderId(), request.getShipmentId(), request.getTrackingNumber());

          // Stub implementation for now
          // TODO: Implement actual status transition logic
          // 1. Find order by ID
          // 2. Validate current status allows transition to SHIPPED
          // 3. If already SHIPPED, return success (idempotent)
          // 4. Update status to SHIPPED
          // 5. Store shipment reference
          // 6. Emit OrderEvent (ORDER_SHIPPED)

          log.info("Order marked as SHIPPED - orderId: {}", request.getOrderId());

          return MarkOrderShippedResult.builder()
                    .success(true)
                    .previousStatus("PACKING")
                    .currentStatus("SHIPPED")
                    .build();
     }
}
