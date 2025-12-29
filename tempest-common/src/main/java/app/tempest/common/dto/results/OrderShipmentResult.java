package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of shipping an individual order within a wave.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShipmentResult {
     private Long orderId;
     private Long shipmentId;
     private String trackingNumber;
     private String status;
     private String failureReason;
}

