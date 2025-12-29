package app.tempest.common.dto.requests;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to confirm a shipment has been shipped.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmShipmentRequest {
     private Long shipmentId;
     private Long orderId;
     private Instant shippedAt;
}

