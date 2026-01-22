package app.tempest.oms.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.tempest.common.dto.requests.RandomDAGWorkflowRequest;
import app.tempest.common.security.SecurityUtils;
import app.tempest.oms.temporal.RandomDAGWorkflowClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for the RandomDAGWorkflow demo.
 */
@Slf4j
@RestController
@RequestMapping("/api/demo/random-dag")
@RequiredArgsConstructor
public class RandomDAGController {

    private final RandomDAGWorkflowClient workflowClient;

    /**
     * DTO for the start workflow request.
     */
    public record StartWorkflowRequest(List<String> steps) {
    }

    /**
     * DTO for the start workflow response.
     */
    public record StartWorkflowResponse(String workflowId, String temporalUiUrl) {
    }

    /**
     * DTO for the workflow status response.
     */
    public record WorkflowStatusResponse(String status, String currentStep) {
    }

    /**
     * Start a new RandomDAGWorkflow with the specified step order.
     * 
     * @param request The request containing the ordered list of steps
     * @param jwt     The JWT token for authentication
     * @return The workflow ID and Temporal UI URL
     */
    @PostMapping
    public ResponseEntity<StartWorkflowResponse> startWorkflow(
            @RequestBody StartWorkflowRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String tenantId = SecurityUtils.requireTenantId(jwt);
        String requestId = UUID.randomUUID().toString();

        log.info("Starting RandomDAGWorkflow - tenantId: {}, requestId: {}, steps: {}",
                tenantId, requestId, request.steps());

        RandomDAGWorkflowRequest workflowRequest = RandomDAGWorkflowRequest.builder()
                .requestId(requestId)
                .tenantId(tenantId)
                .steps(request.steps())
                .build();

        String workflowId = workflowClient.startRandomDAGWorkflow(workflowRequest);

        // Generate Temporal UI URL
        String temporalUiUrl = String.format("http://localhost:8080/namespaces/default/workflows/%s", workflowId);

        log.info("RandomDAGWorkflow started - workflowId: {}, temporalUiUrl: {}", workflowId, temporalUiUrl);

        return ResponseEntity.ok(new StartWorkflowResponse(workflowId, temporalUiUrl));
    }

    /**
     * Get the status of a RandomDAGWorkflow.
     * 
     * @param workflowId The workflow ID
     * @param jwt        The JWT token for authentication
     * @return The workflow status
     */
    @GetMapping("/{workflowId}/status")
    public ResponseEntity<WorkflowStatusResponse> getStatus(
            @PathVariable String workflowId,
            @AuthenticationPrincipal Jwt jwt) {

        SecurityUtils.requireTenantId(jwt);

        try {
            String status = workflowClient.getStatus(workflowId);
            String currentStep = workflowClient.getCurrentStep(workflowId);

            return ResponseEntity.ok(new WorkflowStatusResponse(status, currentStep));
        } catch (Exception e) {
            log.error("Failed to get workflow status - workflowId: {}", workflowId, e);
            return ResponseEntity.notFound().build();
        }
    }
}
