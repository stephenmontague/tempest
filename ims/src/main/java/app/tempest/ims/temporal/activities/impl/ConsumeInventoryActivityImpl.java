package app.tempest.ims.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.ConsumeInventoryRequest;
import app.tempest.common.dto.results.ConsumeInventoryResult;
import app.tempest.ims.temporal.activities.ConsumeInventoryActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConsumeInventoryActivityImpl implements ConsumeInventoryActivity {

     @Override
     public ConsumeInventoryResult consume(ConsumeInventoryRequest consumeInventoryRequest) {
          // Stub implementation for now
          // TODO: Implement actual consumption logic
          // 1. Validate reservation exists
          // 2. Decrement availableQuantity on InventoryBalance
          // 3. Decrement reservedQuantity on InventoryBalance
          // 4. Mark reservation as CONSUMED

          log.info("Consuming {} of {} for order {}, reservation {}",
                    consumeInventoryRequest.getQuantity(),
                    consumeInventoryRequest.getSku(),
                    consumeInventoryRequest.getOrderId(),
                    consumeInventoryRequest.getReservationId());

          return ConsumeInventoryResult.builder()
                    .success(true)
                    .sku(consumeInventoryRequest.getSku())
                    .quantityConsumed(consumeInventoryRequest.getQuantity())
                    .build();
     }
}
