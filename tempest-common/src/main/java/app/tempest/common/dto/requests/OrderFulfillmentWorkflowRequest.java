package app.tempest.common.dto.requests;

import java.util.List;

import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.ShipToDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to start the OrderFulfillmentWorkflow (legacy/express).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFulfillmentWorkflowRequest {
     private Long orderId;
     private String requestId;
     private Long facilityId;
     private List<OrderLineDTO> orderLines;
     private ShipToDTO shipTo;
}

