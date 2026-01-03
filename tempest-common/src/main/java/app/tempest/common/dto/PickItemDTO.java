package app.tempest.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pick item information for warehouse operations.
 * Each item is associated with an order for tracking within a wave.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickItemDTO {
     private Long orderId;
     private String sku;
     private int quantity;
}
