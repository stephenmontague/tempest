package app.tempest.common.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Current status of a wave execution, returned by workflow queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveStatusDTO {
     private Long waveId;
     private String status;
     private String currentStep;
     private String blockingReason;
     private int totalOrders;
     private int ordersAllocated;
     private int ordersPicked;
     private int ordersPacked;
     private int ordersShipped;
     private int ordersFailed;
     private Map<Long, String> orderStatuses;
     private List<Long> failedOrderIds;
}

