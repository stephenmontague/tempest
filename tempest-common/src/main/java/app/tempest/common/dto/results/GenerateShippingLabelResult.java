package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of shipping label generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateShippingLabelResult {
     private Long labelId;
     private String trackingNumber;
     private String labelUrl;
}

