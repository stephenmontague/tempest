package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to consume allocated inventory after picking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeInventoryRequest {
     private String orderId;
     private String reservationId;
     private String sku;
     private int quantity;
}

