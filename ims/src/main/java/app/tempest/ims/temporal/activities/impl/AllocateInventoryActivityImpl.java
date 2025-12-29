package app.tempest.ims.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.AllocateInventoryRequest;
import app.tempest.common.dto.results.AllocateInventoryResult;
import app.tempest.ims.temporal.activities.AllocateInventoryActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AllocateInventoryActivityImpl implements AllocateInventoryActivity {

     @Override
     public AllocateInventoryResult allocate(AllocateInventoryRequest allocateInventoryRequest) {
          // Stub implementation for now
          // TODO: Implement actual reservation logic
          // 1. Check if SKU is a KIT, expand BOM if needed
          // 2. Check availability
          // 3. Create InventoryReservation record
          // 4. Increment reservedQuantity on InventoryBalance

          String reservationId = "reservation-" + allocateInventoryRequest.getOrderId() + "-"
                    + allocateInventoryRequest.getSku();

          log.info("Allocation {} of {} for order {}", allocateInventoryRequest.getQuantity(),
                    allocateInventoryRequest.getSku(), allocateInventoryRequest.getOrderId());

          return AllocateInventoryResult.builder()
                    .reservationId(reservationId)
                    .success(true)
                    .sku(allocateInventoryRequest.getSku())
                    .quantityAllocated(allocateInventoryRequest.getQuantity())
                    .build();
     }
}
