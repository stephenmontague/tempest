package app.tempest.common.dto.requests;

import java.util.List;

import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.ShipToDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to validate an order before creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOrderRequest {
     private String requestId;
     private String externalOrderId;
     private String channel;
     private List<OrderLineDTO> orderLines;
     private ShipToDTO shipTo;
}

