package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to allocate inventory for an order line.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateInventoryRequest {
     private Long orderId;
     private String sku;
     private int quantity;
}

