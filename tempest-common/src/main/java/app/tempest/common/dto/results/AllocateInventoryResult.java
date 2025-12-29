package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of inventory allocation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateInventoryResult {
     private String reservationId;
     private boolean success;
     private String sku;
     private int quantityAllocated;
     private String errorMessage;
}

