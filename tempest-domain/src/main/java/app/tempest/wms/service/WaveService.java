package app.tempest.wms.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.wms.dto.CreateWaveRequest;
import app.tempest.wms.dto.ReleaseWaveRequest;
import app.tempest.wms.dto.WaveResponse;
import app.tempest.wms.entity.Wave;
import app.tempest.wms.entity.Wave.WaveStatus;
import app.tempest.wms.repo.WaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for Wave operations.
 *
 * This is a pure CRUD/system-of-record service: it owns wave rows and their lifecycle
 * status. The WaveExecutionWorkflow is started, signalled, and queried by the UI Temporal
 * client (the single Temporal client tier) — not here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaveService {

     private final WaveRepository waveRepository;

     /**
      * Create a new wave with the specified orders. Simple CRUD - no workflow.
      */
     @Transactional
     public WaveResponse createWave(String tenantId, CreateWaveRequest request) {
          log.info("Creating wave for facility {} with {} orders",
                    request.getFacilityId(), request.getOrderIds().size());

          String waveNumber = request.getWaveNumber();
          if (waveNumber == null || waveNumber.isBlank()) {
               waveNumber = "WAVE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
          }

          if (waveRepository.existsByTenantIdAndWaveNumber(tenantId, waveNumber)) {
               throw new IllegalArgumentException("Wave number already exists: " + waveNumber);
          }

          Wave wave = Wave.builder()
                    .tenantId(tenantId)
                    .facilityId(request.getFacilityId())
                    .waveNumber(waveNumber)
                    .status(WaveStatus.CREATED)
                    .orderIds(request.getOrderIds())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

          wave = waveRepository.save(wave);
          log.info("Wave created - waveId: {}, waveNumber: {}", wave.getId(), wave.getWaveNumber());

          return toResponse(wave);
     }

     /**
      * Get a wave by ID.
      */
     @Transactional(readOnly = true)
     public WaveResponse getWave(String tenantId, Long waveId) {
          Wave wave = waveRepository.findByTenantIdAndId(tenantId, waveId)
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + waveId));
          return toResponse(wave);
     }

     /**
      * Get all waves for a facility.
      */
     @Transactional(readOnly = true)
     public List<WaveResponse> getWavesByFacility(String tenantId, Long facilityId) {
          return waveRepository.findByTenantIdAndFacilityId(tenantId, facilityId)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
     }

     /**
      * Get all waves with a specific status.
      */
     @Transactional(readOnly = true)
     public List<WaveResponse> getWavesByStatus(String tenantId, WaveStatus status) {
          return waveRepository.findByTenantIdAndStatus(tenantId, status)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
     }

     /**
      * Get all waves for a tenant.
      */
     @Transactional(readOnly = true)
     public List<WaveResponse> getAllWaves(String tenantId) {
          return waveRepository.findByTenantId(tenantId)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
     }

     /**
      * Release a wave for execution.
      *
      * CRUD only: this transitions the wave to RELEASED and records the (deterministic)
      * workflow id. The WaveExecutionWorkflow itself is started by the UI Temporal client.
      */
     @Transactional
     public WaveResponse releaseWave(String tenantId, Long waveId, ReleaseWaveRequest request) {
          Wave wave = waveRepository.findByTenantIdAndId(tenantId, waveId)
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + waveId));

          if (wave.getStatus() != WaveStatus.CREATED) {
               throw new IllegalStateException("Wave cannot be released - current status: " + wave.getStatus());
          }

          String workflowId = "wave-execution-" + wave.getId();
          wave.setStatus(WaveStatus.RELEASED);
          wave.setWorkflowId(workflowId);
          wave.setUpdatedAt(Instant.now());
          wave = waveRepository.save(wave);

          log.info("Wave marked RELEASED - waveId: {}, workflowId: {} (workflow started by UI client)",
                    wave.getId(), workflowId);

          return toResponse(wave);
     }

     /**
      * Cancel a wave.
      *
      * CRUD only: the cancel signal (which triggers workflow compensation) is sent to the
      * workflow by the UI Temporal client. Here we just record the cancelled state.
      */
     @Transactional
     public WaveResponse cancelWave(String tenantId, Long waveId, String reason) {
          Wave wave = waveRepository.findByTenantIdAndId(tenantId, waveId)
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + waveId));

          if (wave.getStatus() == WaveStatus.COMPLETED || wave.getStatus() == WaveStatus.CANCELLED) {
               throw new IllegalStateException("Wave cannot be cancelled - current status: " + wave.getStatus());
          }

          log.info("Marking wave CANCELLED - waveId: {}, reason: {}", waveId, reason);
          wave.setStatus(WaveStatus.CANCELLED);
          wave.setUpdatedAt(Instant.now());
          wave = waveRepository.save(wave);

          return toResponse(wave);
     }

     private WaveResponse toResponse(Wave wave) {
          return WaveResponse.builder()
                    .id(wave.getId())
                    .facilityId(wave.getFacilityId())
                    .waveNumber(wave.getWaveNumber())
                    .status(wave.getStatus().name())
                    .orderIds(wave.getOrderIds())
                    .workflowId(wave.getWorkflowId())
                    .createdAt(wave.getCreatedAt())
                    .updatedAt(wave.getUpdatedAt())
                    .build();
     }
}
