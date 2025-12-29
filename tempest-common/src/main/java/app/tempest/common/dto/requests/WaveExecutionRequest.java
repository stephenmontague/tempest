package app.tempest.common.dto.requests;

import java.util.List;

import app.tempest.common.dto.WaveOrderDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to execute a wave containing one or more orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveExecutionRequest {
     private Long waveId;
     private Long facilityId;
     private String waveNumber;
     private List<WaveOrderDTO> orders;
}

