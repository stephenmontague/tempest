package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to mark an order as shipped.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkOrderShippedRequest {
     private Long orderId;
     private Long shipmentId;
     private String trackingNumber;
     private String carrier;
}

