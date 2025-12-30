package app.tempest.wms.dto;

import java.util.Map;

import app.tempest.common.dto.ShipmentStateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for shipment states in a wave.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatesResponse {
     private Map<Long, ShipmentStateDTO> shipments;
}

