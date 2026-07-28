package app.tempest.wms.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to release a wave for execution.
 * Contains order details needed for the workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseWaveRequest {

     /**
      * Order details for each order in the wave.
      * These are fetched from OMS and passed to the workflow.
      */
     private List<WaveOrderDetail> orders;

     /**
      * Fulfillment mode controlling HITL behavior.
      * - STANDARD (default): Full HITL with rate shopping, manual label, manual ship confirm
      * - EXPRESS: Skip rate shopping, use default carrier, manual label and ship confirm
      * - AUTO_SHIP: Fully automated after packing - auto label and auto ship confirm
      */
     @Builder.Default
     private String fulfillmentMode = "STANDARD";

     /**
      * Default carrier to use for EXPRESS and AUTO_SHIP modes.
      * Required when fulfillmentMode is not STANDARD.
      */
     private String defaultCarrier;

     /**
      * Default service level to use for EXPRESS and AUTO_SHIP modes.
      * Required when fulfillmentMode is not STANDARD.
      */
     private String defaultServiceLevel;
}
