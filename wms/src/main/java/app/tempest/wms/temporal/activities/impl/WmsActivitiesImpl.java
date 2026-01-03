package app.tempest.wms.temporal.activities.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.CreatePickWaveRequest;
import app.tempest.common.dto.results.CreatePickWaveResult;
import app.tempest.common.temporal.activities.wms.WmsActivities;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of WmsActivities for remote calls from other services.
 * This is registered on the wms-tasks queue and handles cross-service activity calls.
 */
@Slf4j
@Component
public class WmsActivitiesImpl implements WmsActivities {

    @Override
    public CreatePickWaveResult createPickWave(CreatePickWaveRequest request) {
        log.info("Creating pick tasks for wave - waveId: {}, facilityId: {}, strategy: {}, items: {}",
                request.getWaveId(), request.getFacilityId(), request.getStrategy(),
                request.getItems() != null ? request.getItems().size() : 0);

        // Stub implementation for now
        // TODO: Implement actual wave creation logic
        // 1. Check if pick tasks for waveId already exist (idempotency)
        // 2. If exists, return existing pick task IDs
        // 3. Validate facility exists and is active
        // 4. Create PickTask entities for each item (grouped by orderId)
        // 5. Return pick task details

        // Simulate pick task ID generation
        Long baseId = System.currentTimeMillis();
        int itemCount = request.getItems() != null ? request.getItems().size() : 0;
        List<Long> pickTaskIds = new java.util.ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            pickTaskIds.add(baseId + i);
        }

        log.info("Pick tasks created for wave {} - pickTasks: {}", request.getWaveId(), pickTaskIds.size());

        return CreatePickWaveResult.builder()
                .waveId(request.getWaveId())
                .status("CREATED")
                .pickTaskIds(pickTaskIds)
                .alreadyExisted(false)
                .build();
    }
}

