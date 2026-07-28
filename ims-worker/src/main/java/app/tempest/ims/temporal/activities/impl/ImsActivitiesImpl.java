package app.tempest.ims.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.AllocateInventoryRequest;
import app.tempest.common.dto.requests.ConsumeInventoryRequest;
import app.tempest.common.dto.requests.ReleaseInventoryRequest;
import app.tempest.common.dto.results.AllocateInventoryResult;
import app.tempest.common.dto.results.ConsumeInventoryResult;
import app.tempest.common.dto.results.ReleaseInventoryResult;
import app.tempest.common.temporal.activities.ims.ImsActivities;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ImsActivities for remote calls from other services.
 * This is registered on the ims-tasks queue and handles cross-service activity calls.
 */
@Slf4j
@Component
public class ImsActivitiesImpl implements ImsActivities {

    @Override
    public AllocateInventoryResult allocate(AllocateInventoryRequest request) {
        // Stub implementation for now
        // TODO: Implement actual reservation logic
        // 1. Check if SKU is a KIT, expand BOM if needed
        // 2. Check availability
        // 3. Create InventoryReservation record
        // 4. Increment reservedQuantity on InventoryBalance

        String reservationId = "reservation-" + request.getOrderId() + "-" + request.getSku();

        log.info("Allocation {} of {} for order {}", request.getQuantity(),
                request.getSku(), request.getOrderId());

        return AllocateInventoryResult.builder()
                .reservationId(reservationId)
                .success(true)
                .sku(request.getSku())
                .quantityAllocated(request.getQuantity())
                .build();
    }

    @Override
    public ReleaseInventoryResult releaseInventory(ReleaseInventoryRequest request) {
        // Stub implementation for now
        // TODO: Implement actual release logic
        // 1. Find reservation by ID
        // 2. Decrement reservedQuantity on InventoryBalance
        // 3. Mark reservation as RELEASED

        log.info("Releasing reservation {} for order {}, reason: {}",
                request.getReservationId(),
                request.getOrderId(),
                request.getReason());

        return ReleaseInventoryResult.builder()
                .success(true)
                .reservationId(request.getReservationId())
                .build();
    }

    @Override
    public ConsumeInventoryResult consumeInventory(ConsumeInventoryRequest request) {
        // Stub implementation for now
        // TODO: Implement actual consumption logic
        // 1. Validate reservation exists
        // 2. Decrement availableQuantity on InventoryBalance
        // 3. Decrement reservedQuantity on InventoryBalance
        // 4. Mark reservation as CONSUMED

        log.info("Consuming {} of {} for order {}, reservation {}",
                request.getQuantity(),
                request.getSku(),
                request.getOrderId(),
                request.getReservationId());

        return ConsumeInventoryResult.builder()
                .success(true)
                .sku(request.getSku())
                .quantityConsumed(request.getQuantity())
                .build();
    }
}

