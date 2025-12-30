package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to update wave status in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWaveStatusRequest {
     private String tenantId;
     private Long waveId;
     private String status;
}

