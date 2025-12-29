package app.tempest.common.dto.requests;

import java.util.List;

import app.tempest.common.dto.OrderLineDTO;
import app.tempest.common.dto.ShipToDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to start the OrderIntakeWorkflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderIntakeWorkflowRequest {
     private String requestId;
     private String externalOrderId;
     private String channel;
     private String priority;
     private Long facilityId;
     private List<OrderLineDTO> orderLines;
     private ShipToDTO shipTo;
}

