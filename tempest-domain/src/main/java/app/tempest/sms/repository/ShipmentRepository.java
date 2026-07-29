package app.tempest.sms.repository;

import app.tempest.sms.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Shipment entities.
 * All queries MUST be tenant-scoped to enforce multi-tenant isolation.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Find all shipments for a specific tenant.
     */
    List<Shipment> findByTenantId(String tenantId);

    /**
     * Find shipments by status for a specific tenant.
     */
    List<Shipment> findByTenantIdAndStatus(String tenantId, String status);

    /**
     * Find a shipment by ID and tenant ID.
     * This ensures tenant isolation - users can only access their own tenant's shipments.
     */
    Optional<Shipment> findByIdAndTenantId(Long id, String tenantId);

    /**
     * Find shipments by order ID within a tenant.
     */
    List<Shipment> findByTenantIdAndOrderId(String tenantId, Long orderId);

    /**
     * Find a shipment by tracking number within a tenant.
     */
    Optional<Shipment> findByTenantIdAndTrackingNumber(String tenantId, String trackingNumber);
}

