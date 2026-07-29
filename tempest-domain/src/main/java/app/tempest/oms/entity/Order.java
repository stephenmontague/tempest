package app.tempest.oms.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import app.tempest.common.entity.TenantAwareEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Order entity representing a customer order.
 * All orders are tenant-scoped - external order ID uniqueness is enforced per
 * tenant.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders", uniqueConstraints = {
          @UniqueConstraint(name = "uk_orders_tenant_external_id", columnNames = { "tenant_id", "external_order_id" })
}, indexes = {
          @Index(name = "idx_orders_tenant_id", columnList = "tenant_id"),
          @Index(name = "idx_orders_status", columnList = "tenant_id, status")
})
public class Order extends TenantAwareEntity {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(name = "external_order_id", nullable = false)
     private String externalOrderId;

     @Column(name = "status", nullable = false)
     @Builder.Default
     private String status = "CREATED";

     @Column(name = "customer_email")
     private String customerEmail;

     @Column(name = "customer_name")
     private String customerName;

     @Column(name = "shipping_address_line1")
     private String shippingAddressLine1;

     @Column(name = "shipping_address_line2")
     private String shippingAddressLine2;

     @Column(name = "shipping_city")
     private String shippingCity;

     @Column(name = "shipping_state")
     private String shippingState;

     @Column(name = "shipping_postal_code")
     private String shippingPostalCode;

     @Column(name = "shipping_country")
     private String shippingCountry;

     @Column(name = "workflow_id")
     private String workflowId;

     @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @Builder.Default
     @JsonIgnore // Prevent circular reference during JSON serialization (Order -> OrderLine -> Order)
     private List<OrderLine> orderLines = new ArrayList<>();
}
