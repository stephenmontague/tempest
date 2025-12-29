package app.tempest.common.dto.results;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of pick wave creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePickWaveResult {
     private Long waveId;
     private String status;
     private List<Long> pickTaskIds;
     private boolean alreadyExisted;
}

