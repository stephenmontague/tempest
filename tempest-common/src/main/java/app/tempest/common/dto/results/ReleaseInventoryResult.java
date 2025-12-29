package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of inventory release.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryResult {
     private boolean success;
     private String reservationId;
}

