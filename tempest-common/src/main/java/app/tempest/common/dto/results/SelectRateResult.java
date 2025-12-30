package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of selecting a shipping rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectRateResult {
     private Long shipmentId;
     private String carrier;
     private String serviceLevel;
     private boolean success;
}

