package app.tempest.ims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Base class for all tenant-scoped entities.
 * Provides:
 * - tenantId: Required for multi-tenant isolation
 * - createdByUserId / updatedByUserId: Audit trail
 * - createdAt / updatedAt: Timestamps
 * 
 * All queries and writes MUST be scoped by tenantId.
 * tenantId is derived from JWT claims, never from request bodies.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantAwareEntity {

    /**
     * Tenant identifier - REQUIRED for all records.
     * Must be derived from JWT tenant_id claim, never from request body.
     */
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    /**
     * User ID who created this record (from JWT sub claim).
     */
    @Column(name = "created_by_user_id", updatable = false)
    private String createdByUserId;

    /**
     * User ID who last updated this record (from JWT sub claim).
     */
    @Column(name = "updated_by_user_id")
    private String updatedByUserId;

    /**
     * Timestamp when this record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when this record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

