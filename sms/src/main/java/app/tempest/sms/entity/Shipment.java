package app.tempest.sms.entity;

import java.time.Instant;

import app.tempest.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shipment entity representing a package shipment.
 * All shipments are tenant-scoped.
 * 
 * Status flow: CREATED -> LABEL_GENERATED -> SHIPPED -> IN_TRANSIT -> DELIVERED
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_shipments_order_id", columnList = "order_id")
})
public class Shipment extends TenantAwareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "carrier", nullable = false)
    private String carrier;

    @Column(name = "service_level", nullable = false)
    private String serviceLevel;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "CREATED";

    @Column(name = "label_url")
    private String labelUrl;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;
}
