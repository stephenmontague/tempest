package app.tempest.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pick item information for warehouse operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickItemDTO {
     private String sku;
     private int quantity;
}
