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
}
