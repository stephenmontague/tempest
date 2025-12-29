package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to generate a shipping label.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateShippingLabelRequest {
     private Long shipmentId;
     private Long orderId;
     private String carrier;
     private String serviceLevel;
}

