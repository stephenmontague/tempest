package app.tempest.common.dto.results;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of the RandomDAGWorkflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomDAGWorkflowResult {
    private String demoId;
    private String status;
    private List<String> completedSteps;
}
