package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of updating wave status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWaveStatusResult {
     private Long waveId;
     private String previousStatus;
     private String newStatus;
     private boolean success;
}

