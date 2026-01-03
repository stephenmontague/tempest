package app.tempest.common.dto.requests;

import java.util.List;

import app.tempest.common.dto.WaveOrderDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to execute a wave containing one or more orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveExecutionRequest {
     private String tenantId;
     private Long waveId;
     private Long facilityId;
     private String waveNumber;
     private List<WaveOrderDTO> orders;

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
      */
     private String defaultCarrier;

     /**
      * Default service level to use for EXPRESS and AUTO_SHIP modes.
      */
     private String defaultServiceLevel;
}

