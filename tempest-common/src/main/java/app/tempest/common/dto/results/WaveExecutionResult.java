package app.tempest.common.dto.results;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of wave execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveExecutionResult {
     private Long waveId;
     private String finalStatus;
     private int totalOrders;
     private int successfulOrders;
     private int failedOrders;
     private List<OrderShipmentResult> orderShipments;
}

