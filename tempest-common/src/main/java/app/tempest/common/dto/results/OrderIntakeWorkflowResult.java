package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of the OrderIntakeWorkflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderIntakeWorkflowResult {
     private Long orderId;
     private String status;
}

