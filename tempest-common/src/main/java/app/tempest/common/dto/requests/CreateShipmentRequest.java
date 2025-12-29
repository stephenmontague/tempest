package app.tempest.common.dto.requests;

import app.tempest.common.dto.ShipToDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create a shipment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {
     private Long orderId;
     private Long facilityId;
     private String carrier;
     private String serviceLevel;
     private ShipToDTO shipTo;
}

