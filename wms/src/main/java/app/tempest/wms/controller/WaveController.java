package app.tempest.wms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.tempest.wms.dto.CreateWaveRequest;
import app.tempest.wms.dto.ReleaseWaveRequest;
import app.tempest.wms.dto.SelectRateRequest;
import app.tempest.wms.dto.ShipmentStatesResponse;
import app.tempest.wms.dto.WaveResponse;
import app.tempest.wms.dto.WorkflowStatusResponse;
import app.tempest.wms.entity.Wave.WaveStatus;
import app.tempest.wms.service.WaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/waves")
@RequiredArgsConstructor
public class WaveController {

     private final WaveService waveService;

     /**
      * Create a new wave with the specified orders.
      */
     @PostMapping
     public ResponseEntity<WaveResponse> createWave(
               @AuthenticationPrincipal Jwt jwt,
               @Valid @RequestBody CreateWaveRequest request) {

          String tenantId = extractTenantId(jwt);
          log.info("Creating wave - tenantId: {}, facilityId: {}, orderCount: {}",
                    tenantId, request.getFacilityId(), request.getOrderIds().size());

          WaveResponse response = waveService.createWave(tenantId, request);
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
     }

     /**
      * Get a wave by ID.
      */
     @GetMapping("/{waveId}")
     public ResponseEntity<WaveResponse> getWave(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId) {

          String tenantId = extractTenantId(jwt);
          WaveResponse response = waveService.getWave(tenantId, waveId);
          return ResponseEntity.ok(response);
     }

     /**
      * Get all waves for a facility.
      */
     @GetMapping
     public ResponseEntity<List<WaveResponse>> getWaves(
               @AuthenticationPrincipal Jwt jwt,
               @RequestParam(required = false) Long facilityId,
               @RequestParam(required = false) String status) {

          String tenantId = extractTenantId(jwt);

          List<WaveResponse> waves;
          if (status != null) {
               WaveStatus waveStatus = WaveStatus.valueOf(status.toUpperCase());
               waves = waveService.getWavesByStatus(tenantId, waveStatus);
          } else if (facilityId != null) {
               waves = waveService.getWavesByFacility(tenantId, facilityId);
          } else {
               // Return all waves for tenant (could add pagination)
               waves = waveService.getAllWaves(tenantId);
          }

          return ResponseEntity.ok(waves);
     }

     /**
      * Release a wave for execution.
      * This starts the WaveExecutionWorkflow.
      */
     @PostMapping("/{waveId}/release")
     public ResponseEntity<WaveResponse> releaseWave(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId,
               @RequestBody ReleaseWaveRequest request) {

          String tenantId = extractTenantId(jwt);
          log.info("Releasing wave - tenantId: {}, waveId: {}", tenantId, waveId);

          WaveResponse response = waveService.releaseWave(tenantId, waveId, request);
          return ResponseEntity.ok(response);
     }

     /**
      * Cancel a wave.
      */
     @DeleteMapping("/{waveId}")
     public ResponseEntity<WaveResponse> cancelWave(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId,
               @RequestParam(required = false, defaultValue = "Cancelled by user") String reason) {

          String tenantId = extractTenantId(jwt);
          log.info("Cancelling wave - tenantId: {}, waveId: {}, reason: {}", tenantId, waveId, reason);

          WaveResponse response = waveService.cancelWave(tenantId, waveId, reason);
          return ResponseEntity.ok(response);
     }

     /**
      * Signal that all picks in a wave are completed.
      */
     @PostMapping("/{waveId}/picks-completed")
     public ResponseEntity<Void> signalPicksCompleted(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId) {

          String tenantId = extractTenantId(jwt);
          log.info("Signaling picks completed - tenantId: {}, waveId: {}", tenantId, waveId);

          waveService.signalPicksCompleted(tenantId, waveId);
          return ResponseEntity.ok().build();
     }

     /**
      * Signal that all packs in a wave are completed.
      */
     @PostMapping("/{waveId}/packs-completed")
     public ResponseEntity<Void> signalPacksCompleted(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId) {

          String tenantId = extractTenantId(jwt);
          log.info("Signaling packs completed - tenantId: {}, waveId: {}", tenantId, waveId);

          waveService.signalPacksCompleted(tenantId, waveId);
          return ResponseEntity.ok().build();
     }

     /**
      * Get the workflow status for a wave.
      */
     @GetMapping("/{waveId}/status")
     public ResponseEntity<WorkflowStatusResponse> getWorkflowStatus(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId) {

          String tenantId = extractTenantId(jwt);
          WorkflowStatusResponse status = waveService.getWorkflowStatus(tenantId, waveId);
          return ResponseEntity.ok(status);
     }

     /**
      * Get shipment states for a wave.
      */
     @GetMapping("/{waveId}/shipments")
     public ResponseEntity<ShipmentStatesResponse> getShipmentStates(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId) {

          String tenantId = extractTenantId(jwt);
          log.info("Getting shipment states - tenantId: {}, waveId: {}", tenantId, waveId);

          ShipmentStatesResponse response = waveService.getShipmentStates(tenantId, waveId);
          return ResponseEntity.ok(response);
     }

     /**
      * Signal rate selection for a shipment in a wave.
      */
     @PostMapping("/{waveId}/shipments/{shipmentId}/select-rate")
     public ResponseEntity<Void> signalRateSelected(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId,
               @PathVariable Long shipmentId,
               @RequestBody SelectRateRequest request) {

          String tenantId = extractTenantId(jwt);
          log.info("Signaling rate selected - tenantId: {}, waveId: {}, shipmentId: {}, carrier: {}", 
                    tenantId, waveId, shipmentId, request.getCarrier());

          waveService.signalRateSelected(tenantId, waveId, shipmentId, request.getCarrier(), request.getServiceLevel());
          return ResponseEntity.ok().build();
     }

     /**
      * Signal to print label for a shipment in a wave.
      */
     @PostMapping("/{waveId}/shipments/{shipmentId}/print-label")
     public ResponseEntity<Void> signalPrintLabel(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId,
               @PathVariable Long shipmentId) {

          String tenantId = extractTenantId(jwt);
          log.info("Signaling print label - tenantId: {}, waveId: {}, shipmentId: {}", tenantId, waveId, shipmentId);

          waveService.signalPrintLabel(tenantId, waveId, shipmentId);
          return ResponseEntity.ok().build();
     }

     /**
      * Signal that a shipment has been confirmed as shipped.
      */
     @PostMapping("/{waveId}/shipments/{shipmentId}/confirm-shipped")
     public ResponseEntity<Void> signalShipmentConfirmed(
               @AuthenticationPrincipal Jwt jwt,
               @PathVariable Long waveId,
               @PathVariable Long shipmentId) {

          String tenantId = extractTenantId(jwt);
          log.info("Signaling shipment confirmed - tenantId: {}, waveId: {}, shipmentId: {}", 
                    tenantId, waveId, shipmentId);

          waveService.signalShipmentConfirmed(tenantId, waveId, shipmentId);
          return ResponseEntity.ok().build();
     }

     private String extractTenantId(Jwt jwt) {
          // Extract tenant_id from JWT claims
          String tenantId = jwt.getClaimAsString("tenant_id");
          if (tenantId == null || tenantId.isBlank()) {
               throw new IllegalStateException("tenant_id claim is missing from JWT");
          }
          return tenantId;
     }
}
