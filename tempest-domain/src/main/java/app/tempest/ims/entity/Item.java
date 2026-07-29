package app.tempest.ims.entity;

import app.tempest.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item entity representing a product/SKU in the inventory system.
 * All items are tenant-scoped - SKU uniqueness is enforced per tenant.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "items", uniqueConstraints = {
                @UniqueConstraint(name = "uk_items_tenant_sku", columnNames = { "tenant_id", "sku" })
}, indexes = {
                @Index(name = "idx_items_tenant_id", columnList = "tenant_id")
})
public class Item extends TenantAwareEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "sku", nullable = false)
        private String sku;

        @Column(name = "name", nullable = false)
        private String name;

        @Column(name = "description")
        private String description;

        @Column(name = "active", nullable = false)
        @Builder.Default
        private boolean active = true;
}
