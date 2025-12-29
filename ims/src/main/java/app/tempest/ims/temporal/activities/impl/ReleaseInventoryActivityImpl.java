package app.tempest.ims.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.ReleaseInventoryRequest;
import app.tempest.common.dto.results.ReleaseInventoryResult;
import app.tempest.ims.temporal.activities.ReleaseInventoryActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReleaseInventoryActivityImpl implements ReleaseInventoryActivity {

     @Override
     public ReleaseInventoryResult release(ReleaseInventoryRequest releaseInventoryRequest) {
          // Stub implementation for now
          // TODO: Implement actual release logic
          // 1. Find reservation by ID
          // 2. Decrement reservedQuantity on InventoryBalance
          // 3. Mark reservation as RELEASED

          log.info("Releasing reservation {} for order {}, reason: {}",
                    releaseInventoryRequest.getReservationId(),
                    releaseInventoryRequest.getOrderId(),
                    releaseInventoryRequest.getReason());

          return ReleaseInventoryResult.builder()
                    .success(true)
                    .reservationId(releaseInventoryRequest.getReservationId())
                    .build();
     }
}
