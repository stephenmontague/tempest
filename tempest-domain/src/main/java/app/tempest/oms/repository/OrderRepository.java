package app.tempest.oms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.tempest.oms.entity.Order;

/**
 * Repository for Order entities.
 * All queries MUST be tenant-scoped to enforce multi-tenant isolation.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

     List<Order> findByTenantIdOrderByCreatedAtDesc(String tenantId);

     List<Order> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, String status);

     /**
      * Find orders that contain order lines with the given SKU.
      */
     @Query("SELECT DISTINCT o FROM Order o JOIN o.orderLines ol WHERE o.tenantId = :tenantId AND ol.sku = :sku ORDER BY o.createdAt DESC")
     List<Order> findByTenantIdAndOrderLinesSku(@Param("tenantId") String tenantId, @Param("sku") String sku);

     Optional<Order> findByIdAndTenantId(Long id, String tenantId);

     Optional<Order> findByTenantIdAndExternalOrderId(String tenantId, String externalOrderId);

     Optional<Order> findByWorkflowId(String workflowId);

     boolean existsByTenantIdAndExternalOrderId(String tenantId, String externalOrderId);

     long countByTenantIdAndStatus(String tenantId, String status);
}
