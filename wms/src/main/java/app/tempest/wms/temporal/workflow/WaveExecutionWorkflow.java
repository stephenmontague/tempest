package app.tempest.wms.temporal.workflow;

import app.tempest.common.dto.WaveStatusDTO;
import app.tempest.common.dto.requests.WaveExecutionRequest;
import app.tempest.common.dto.results.WaveExecutionResult;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow for executing a wave of orders through fulfillment.
 * 
 * A wave is a batch of orders that are processed together for warehouse
 * efficiency.
 * This workflow orchestrates:
 * 1. Inventory allocation for all orders
 * 2. Pick task creation and completion
 * 3. Packing completion
 * 4. Shipment creation and confirmation
 * 
 * The workflow waits for human-driven signals (picks completed, packs
 * completed)
 * and can be cancelled at any point with proper compensation.
 */
@WorkflowInterface
public interface WaveExecutionWorkflow {

     /**
      * Execute the wave fulfillment process.
      * 
      * @param request The wave execution request containing all orders to fulfill
      * @return The result of wave execution including shipment details
      */
     @WorkflowMethod
     WaveExecutionResult execute(WaveExecutionRequest request);

     /**
      * Signal that all picks in the wave have been completed.
      * This unblocks the workflow to proceed to inventory consumption and packing.
      */
     @SignalMethod
     void allPicksCompleted();

     /**
      * Signal that all packs in the wave have been completed.
      * This unblocks the workflow to proceed to shipment creation.
      */
     @SignalMethod
     void allPacksCompleted();

     /**
      * Signal to cancel the wave execution.
      * This will trigger compensation (release inventory, cancel shipments).
      * 
      * @param reason The reason for cancellation
      */
     @SignalMethod
     void cancelWave(String reason);

     /**
      * Signal that a specific order's pick tasks are complete.
      * Used for tracking per-order progress within the wave.
      * 
      * @param orderId The order ID that has completed picking
      */
     @SignalMethod
     void orderPickCompleted(Long orderId);

     /**
      * Signal that a specific order's packing is complete.
      * Used for tracking per-order progress within the wave.
      * 
      * @param orderId The order ID that has completed packing
      */
     @SignalMethod
     void orderPackCompleted(Long orderId);

     /**
      * Query the current status of the wave execution.
      * 
      * @return The current wave status including per-order progress
      */
     @QueryMethod
     WaveStatusDTO getWaveStatus();

     /**
      * Query the current step the workflow is executing.
      * 
      * @return The current step name
      */
     @QueryMethod
     String getCurrentStep();

     /**
      * Query why the workflow is currently blocked (if applicable).
      * 
      * @return The blocking reason, or null if not blocked
      */
     @QueryMethod
     String getBlockingReason();
}
