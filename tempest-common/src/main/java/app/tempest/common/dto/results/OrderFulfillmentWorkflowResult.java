package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of the OrderFulfillmentWorkflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFulfillmentWorkflowResult {
     private Long orderId;
     private Long shipmentId;
     private String trackingNumber;
     private String finalStatus;
}

