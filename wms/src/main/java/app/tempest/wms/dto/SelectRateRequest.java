package app.tempest.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for selecting a shipping rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectRateRequest {
     private String carrier;
     private String serviceLevel;
}

