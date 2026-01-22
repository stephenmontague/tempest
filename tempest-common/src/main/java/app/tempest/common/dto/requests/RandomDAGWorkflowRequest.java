package app.tempest.common.dto.requests;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to start the RandomDAGWorkflow.
 * Contains the ordered list of steps to execute.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomDAGWorkflowRequest {
    private String requestId;
    private String tenantId;
    private List<String> steps;
}
