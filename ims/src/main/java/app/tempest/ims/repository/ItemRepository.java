package app.tempest.ims.repository;

import app.tempest.ims.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Item entities.
 * All queries MUST be tenant-scoped to enforce multi-tenant isolation.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find all items for a specific tenant.
     */
    List<Item> findByTenantId(String tenantId);

    /**
     * Find all active items for a specific tenant.
     */
    List<Item> findByTenantIdAndActiveTrue(String tenantId);

    /**
     * Find an item by ID and tenant ID.
     * This ensures tenant isolation - users can only access their own tenant's items.
     */
    Optional<Item> findByIdAndTenantId(Long id, String tenantId);

    /**
     * Find an item by SKU within a tenant.
     * SKU is unique per tenant, not globally.
     */
    Optional<Item> findByTenantIdAndSku(String tenantId, String sku);

    /**
     * Check if an item with the given SKU exists for a tenant.
     */
    boolean existsByTenantIdAndSku(String tenantId, String sku);
}
