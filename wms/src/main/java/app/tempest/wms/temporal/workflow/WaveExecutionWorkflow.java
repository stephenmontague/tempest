package app.tempest.wms.temporal.workflow;

import java.util.Map;

import app.tempest.common.dto.FetchedRatesDTO;
import app.tempest.common.dto.ShipmentStateDTO;
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
 * 4. Shipment creation (auto after packs complete)
 * 5. HITL: Rate selection (optional), Print label, Confirm shipped
 * 
 * The workflow waits for human-driven signals (picks completed, packs
 * completed, print label, confirm shipped) and can be cancelled at any point
 * with proper compensation.
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
      * Signal that a rate has been selected for a shipment.
      * This is optional - users can skip rate shopping and use default carrier.
      * 
      * @param shipmentId   The shipment ID
      * @param carrier      The selected carrier
      * @param serviceLevel The selected service level
      */
     @SignalMethod
     void rateSelected(Long shipmentId, String carrier, String serviceLevel);

     /**
      * Signal to print a label for a shipment.
      * This triggers label generation and updates the shipment status.
      * 
      * @param shipmentId The shipment ID to print label for
      */
     @SignalMethod
     void printLabel(Long shipmentId);

     /**
      * Signal that a shipment has been confirmed as shipped.
      * This marks the shipment as complete.
      * 
      * @param shipmentId The shipment ID that has been shipped
      */
     @SignalMethod
     void shipmentConfirmed(Long shipmentId);

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

    /**
     * Query the current state of all shipments in the wave.
     * 
     * @return Map of shipmentId to ShipmentStateDTO
     */
    @QueryMethod
    Map<Long, ShipmentStateDTO> getShipmentStates();

    /**
     * Signal to fetch shipping rates for a shipment.
     * This triggers parallel rate fetching from USPS, UPS, and FedEx.
     * The FedEx call will fail 4 times before succeeding on the 5th attempt
     * to demonstrate Temporal's retry capabilities.
     * 
     * @param shipmentId The shipment ID to fetch rates for
     */
    @SignalMethod
    void fetchRates(Long shipmentId);

    /**
     * Query the fetched rates for a shipment.
     * Returns the current state of rate fetching including per-carrier status.
     * 
     * @param shipmentId The shipment ID to get rates for
     * @return The fetched rates DTO with status and rates
     */
    @QueryMethod
    FetchedRatesDTO getFetchedRates(Long shipmentId);
}
