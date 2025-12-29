package app.tempest.wms.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveResponse {
     private Long id;
     private Long facilityId;
     private String waveNumber;
     private String status;
     private List<Long> orderIds;
     private String workflowId;
     private Instant createdAt;
     private Instant updatedAt;
}
