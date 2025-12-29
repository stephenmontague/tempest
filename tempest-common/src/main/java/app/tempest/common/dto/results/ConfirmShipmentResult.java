package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of shipment confirmation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmShipmentResult {
     private boolean success;
     private String status;
}

