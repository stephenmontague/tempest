package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of inventory consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeInventoryResult {
     private boolean success;
     private String sku;
     private int quantityConsumed;
}

