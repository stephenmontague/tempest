package app.tempest.oms.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import app.tempest.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Order line entity representing a line item within an order.
 * All order lines are tenant-scoped.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_lines", indexes = {
          @Index(name = "idx_order_lines_tenant_id", columnList = "tenant_id"),
          @Index(name = "idx_order_lines_order_id", columnList = "order_id")
})
public class OrderLine extends TenantAwareEntity {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "order_id", nullable = false)
     @JsonIgnore // Prevent circular reference during JSON serialization
     private Order order;

     @Column(name = "sku", nullable = false)
     private String sku;

     @Column(name = "quantity", nullable = false)
     private Integer quantity;

     @Column(name = "unit_price", precision = 10, scale = 2)
     private BigDecimal unitPrice;
}
