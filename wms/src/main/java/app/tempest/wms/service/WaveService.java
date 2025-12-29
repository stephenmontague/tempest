package app.tempest.wms.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.ShipToDTO;
import app.tempest.common.dto.WaveOrderDTO;
import app.tempest.common.dto.requests.WaveExecutionRequest;
import app.tempest.common.temporal.TaskQueues;
import app.tempest.wms.dto.CreateWaveRequest;
import app.tempest.wms.dto.OrderLineDetail;
import app.tempest.wms.dto.ReleaseWaveRequest;
import app.tempest.wms.dto.ShipToDetail;
import app.tempest.wms.dto.WaveOrderDetail;
import app.tempest.wms.dto.WaveResponse;
import app.tempest.wms.entity.Wave;
import app.tempest.wms.entity.Wave.WaveStatus;
import app.tempest.wms.repo.WaveRepository;
import app.tempest.wms.temporal.workflow.WaveExecutionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaveService {

     private final WaveRepository waveRepository;
     private final WorkflowClient workflowClient;

     /**
      * Create a new wave with the specified orders.
      * This is a simple CRUD operation - no workflow is started.
      */
     @Transactional
     public WaveResponse createWave(String tenantId, CreateWaveRequest request) {
          log.info("Creating wave for facility {} with {} orders",
                    request.getFacilityId(), request.getOrderIds().size());

          // Generate wave number if not provided
          String waveNumber = request.getWaveNumber();
          if (waveNumber == null || waveNumber.isBlank()) {
               waveNumber = "WAVE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
          }

          // Check for duplicate wave number
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
      * Release a wave for execution.
      * This starts the WaveExecutionWorkflow.
      */
     @Transactional
     public WaveResponse releaseWave(String tenantId, Long waveId, ReleaseWaveRequest request) {
          Wave wave = waveRepository.findByTenantIdAndId(tenantId, waveId)
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + waveId));

          if (wave.getStatus() != WaveStatus.CREATED) {
               throw new IllegalStateException("Wave cannot be released - current status: " + wave.getStatus());
          }

          // Build workflow request
          List<WaveOrderDTO> waveOrders = request.getOrders().stream()
                    .map(this::toWaveOrderDTO)
                    .collect(Collectors.toList());

          WaveExecutionRequest workflowRequest = WaveExecutionRequest.builder()
                    .waveId(wave.getId())
                    .facilityId(wave.getFacilityId())
                    .waveNumber(wave.getWaveNumber())
                    .orders(waveOrders)
                    .build();

          // Start the workflow
          String workflowId = "wave-execution-" + wave.getId();

          WaveExecutionWorkflow workflow = workflowClient.newWorkflowStub(
                    WaveExecutionWorkflow.class,
                    WorkflowOptions.newBuilder()
                              .setWorkflowId(workflowId)
                              .setTaskQueue(TaskQueues.WMS)
                              .build());

          WorkflowClient.start(workflow::execute, workflowRequest);
          log.info("Started WaveExecutionWorkflow - workflowId: {}, waveId: {}", workflowId, wave.getId());

          // Update wave status
          wave.setStatus(WaveStatus.RELEASED);
          wave.setWorkflowId(workflowId);
          wave.setUpdatedAt(Instant.now());
          wave = waveRepository.save(wave);

          return toResponse(wave);
     }

     /**
      * Cancel a wave.
      */
     @Transactional
     public WaveResponse cancelWave(String tenantId, Long waveId, String reason) {
          Wave wave = waveRepository.findByTenantIdAndId(tenantId, waveId)
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + waveId));

          if (wave.getStatus() == WaveStatus.COMPLETED || wave.getStatus() == WaveStatus.CANCELLED) {
               throw new IllegalStateException("Wave cannot be cancelled - current status: " + wave.getStatus());
          }

          // If workflow is running, signal cancellation
          if (wave.getWorkflowId() != null) {
               try {
                    WaveExecutionWorkflow workflow = workflowClient.newWorkflowStub(
                              WaveExecutionWorkflow.class, wave.getWorkflowId());
                    workflow.cancelWave(reason);
                    log.info("Sent cancel signal to workflow - workflowId: {}", wave.getWorkflowId());
               } catch (Exception e) {
                    log.warn("Failed to signal workflow cancellation: {}", e.getMessage());
               }
          }

          wave.setStatus(WaveStatus.CANCELLED);
          wave.setUpdatedAt(Instant.now());
          wave = waveRepository.save(wave);

          return toResponse(wave);
     }

     /**
      * Signal that all picks in a wave are completed.
      */
     public void signalPicksCompleted(String tenantId, Long waveId) {
          Wave wave = waveRepository.findByTenantIdAndId(tenantId, waveId)
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + waveId));

          if (wave.getWorkflowId() == null) {
               throw new IllegalStateException("Wave has no running workflow");
          }

          WaveExecutionWorkflow workflow = workflowClient.newWorkflowStub(
                    WaveExecutionWorkflow.class, wave.getWorkflowId());
          workflow.allPicksCompleted();
          log.info("Sent allPicksCompleted signal - waveId: {}", waveId);
     }

     /**
      * Signal that all packs in a wave are completed.
      */
     public void signalPacksCompleted(String tenantId, Long waveId) {
          Wave wave = waveRepository.findByTenantIdAndId(tenantId, waveId)
                    .orElseThrow(() -> new IllegalArgumentException("Wave not found: " + waveId));

          if (wave.getWorkflowId() == null) {
               throw new IllegalStateException("Wave has no running workflow");
          }

          WaveExecutionWorkflow workflow = workflowClient.newWorkflowStub(
                    WaveExecutionWorkflow.class, wave.getWorkflowId());
          workflow.allPacksCompleted();
          log.info("Sent allPacksCompleted signal - waveId: {}", waveId);
     }

     private WaveOrderDTO toWaveOrderDTO(WaveOrderDetail order) {
          List<OrderLineDTO> orderLines = order.getOrderLines().stream()
                    .map(this::toOrderLineDTO)
                    .collect(Collectors.toList());

          ShipToDTO shipTo = toShipToDTO(order.getShipTo());

          return WaveOrderDTO.builder()
                    .orderId(order.getOrderId())
                    .externalOrderId(order.getExternalOrderId())
                    .orderLines(orderLines)
                    .shipTo(shipTo)
                    .build();
     }

     private OrderLineDTO toOrderLineDTO(OrderLineDetail line) {
          return OrderLineDTO.builder()
                    .orderLineId(line.getOrderLineId())
                    .sku(line.getSku())
                    .quantity(line.getQuantity())
                    .build();
     }

     private ShipToDTO toShipToDTO(ShipToDetail shipTo) {
          if (shipTo == null) {
               return null;
          }
          return ShipToDTO.builder()
                    .name(shipTo.getName())
                    .addressLine1(shipTo.getAddressLine1())
                    .addressLine2(shipTo.getAddressLine2())
                    .city(shipTo.getCity())
                    .state(shipTo.getState())
                    .postalCode(shipTo.getPostalCode())
                    .country(shipTo.getCountry())
                    .build();
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
