package app.tempest.wms.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "waves")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wave {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(name = "tenant_id", nullable = false)
     private String tenantId;

     @Column(name = "facility_id", nullable = false)
     private Long facilityId;

     @Column(name = "wave_number", nullable = false)
     private String waveNumber;

     @Enumerated(EnumType.STRING)
     @Column(name = "status", nullable = false)
     @Builder.Default
     private WaveStatus status = WaveStatus.CREATED;

     @ElementCollection
     @CollectionTable(name = "wave_orders", joinColumns = @JoinColumn(name = "wave_id"))
     @Column(name = "order_id")
     @Builder.Default
     private List<Long> orderIds = new ArrayList<>();

     @Column(name = "workflow_id")
     private String workflowId;

     @Column(name = "created_by_user_id")
     private String createdByUserId;

     @Column(name = "updated_by_user_id")
     private String updatedByUserId;

     @Column(name = "created_at", nullable = false)
     @Builder.Default
     private Instant createdAt = Instant.now();

     @Column(name = "updated_at", nullable = false)
     @Builder.Default
     private Instant updatedAt = Instant.now();

     public enum WaveStatus {
          CREATED,
          RELEASED,
          IN_PROGRESS,
          COMPLETED,
          CANCELLED
     }
}
