package app.tempest.oms.repository;

import app.tempest.oms.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OrderLine entities.
 * All queries MUST be tenant-scoped to enforce multi-tenant isolation.
 */
@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

     List<OrderLine> findByOrderId(Long orderId);

     List<OrderLine> findByOrderIdAndTenantId(Long orderId, String tenantId);
}
