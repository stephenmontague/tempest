package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to select a shipping rate for a shipment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectRateRequest {
     private String tenantId;
     private Long shipmentId;
     private String carrier;
     private String serviceLevel;
}

