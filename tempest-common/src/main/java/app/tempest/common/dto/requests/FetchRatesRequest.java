package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to fetch shipping rates for a shipment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchRatesRequest {
     private String tenantId;
     private Long shipmentId;
     private Long orderId;
}

