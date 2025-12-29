package app.tempest.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an order within a wave, including all data needed for fulfillment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveOrderDTO {
     private Long orderId;
     private String externalOrderId;
     private List<OrderLineDTO> orderLines;
     private ShipToDTO shipTo;
}

