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
import app.tempest.wms.dto.WaveResponse;
import app.tempest.wms.entity.Wave.WaveStatus;
import app.tempest.wms.service.WaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CRUD/system-of-record endpoints for waves. Workflow start, signals, and queries are
 * performed by the UI Temporal client, not through this controller.
 */
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
      * Get all waves for a facility (optionally filtered by status).
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
               waves = waveService.getAllWaves(tenantId);
          }

          return ResponseEntity.ok(waves);
     }

     /**
      * Release a wave for execution (CRUD transition to RELEASED; the UI client starts
      * the WaveExecutionWorkflow).
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
      * Cancel a wave (CRUD transition to CANCELLED; the UI client signals the workflow).
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

     private String extractTenantId(Jwt jwt) {
          String tenantId = jwt.getClaimAsString("tenant_id");
          if (tenantId == null || tenantId.isBlank()) {
               throw new IllegalStateException("tenant_id claim is missing from JWT");
          }
          return tenantId;
     }
}
