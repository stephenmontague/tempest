package app.tempest.wms.temporal.activities.impl;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.requests.UpdateWaveStatusRequest;
import app.tempest.common.dto.results.UpdateWaveStatusResult;
import app.tempest.wms.entity.Wave;
import app.tempest.wms.entity.Wave.WaveStatus;
import app.tempest.wms.repo.WaveRepository;
import app.tempest.wms.temporal.activities.UpdateWaveStatusActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of UpdateWaveStatusActivity.
 * Updates the wave entity status in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateWaveStatusActivityImpl implements UpdateWaveStatusActivity {

     private final WaveRepository waveRepository;

     @Override
     @Transactional
     public UpdateWaveStatusResult updateStatus(UpdateWaveStatusRequest request) {
          log.info("Updating wave status - tenantId: {}, waveId: {}, newStatus: {}",
                    request.getTenantId(), request.getWaveId(), request.getStatus());

          Wave wave = waveRepository.findByTenantIdAndId(request.getTenantId(), request.getWaveId())
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + request.getWaveId()));

          String previousStatus = wave.getStatus().name();
          WaveStatus newStatus = WaveStatus.valueOf(request.getStatus().toUpperCase());

          wave.setStatus(newStatus);
          wave.setUpdatedAt(Instant.now());
          waveRepository.save(wave);

          log.info("Wave status updated - waveId: {}, {} -> {}", 
                    request.getWaveId(), previousStatus, newStatus);

          return UpdateWaveStatusResult.builder()
                    .waveId(request.getWaveId())
                    .previousStatus(previousStatus)
                    .newStatus(newStatus.name())
                    .success(true)
                    .build();
     }
}

