package app.tempest.sms.controller;

import app.tempest.sms.entity.Shipment;
import app.tempest.sms.repository.ShipmentRepository;
import app.tempest.sms.security.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Shipment operations.
 * All operations are tenant-scoped - tenantId is extracted from JWT, never from request body.
 */
@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);

    private final ShipmentRepository shipmentRepository;

    public ShipmentController(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Get all shipments for the current tenant.
     * Accessible by ADMIN, MANAGER, and INTEGRATION roles.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INTEGRATION')")
    public ResponseEntity<List<Shipment>> getShipments(@AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        log.debug("Fetching shipments for tenant: {}", tenantId);

        List<Shipment> shipments = shipmentRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get a specific shipment by ID.
     * Accessible by ADMIN, MANAGER, and INTEGRATION roles.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INTEGRATION')")
    public ResponseEntity<Shipment> getShipment(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        log.debug("Fetching shipment {} for tenant: {}", id, tenantId);

        return shipmentRepository.findByIdAndTenantId(id, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new shipment.
     * Accessible by ADMIN and MANAGER roles only.
     * tenantId is set from JWT, not from request body.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Shipment> createShipment(@RequestBody CreateShipmentRequest request, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        String userId = SecurityUtils.getUserId(jwt).orElse(null);
        log.info("Creating shipment for order {} for tenant: {} by user: {}", request.orderId(), tenantId, userId);

        Shipment shipment = Shipment.builder()
                .orderId(request.orderId())
                .facilityId(request.facilityId())
                .carrier(request.carrier())
                .serviceLevel(request.serviceLevel())
                .status("CREATED")
                .build();

        // Set tenant and audit fields from JWT - never from request body
        shipment.setTenantId(tenantId);
        shipment.setCreatedByUserId(userId);
        shipment.setUpdatedByUserId(userId);

        Shipment saved = shipmentRepository.save(shipment);
        log.info("Created shipment {} for tenant: {}", saved.getId(), tenantId);

        return ResponseEntity.ok(saved);
    }

    /**
     * Request DTO for creating a shipment.
     * Note: tenantId is NOT included - it comes from JWT.
     */
    public record CreateShipmentRequest(
            Long orderId,
            Long facilityId,
            String carrier,
            String serviceLevel
    ) {}
}
