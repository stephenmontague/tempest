package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of shipment creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentResult {
     private Long shipmentId;
     private String status;
     private boolean alreadyExisted;
}

