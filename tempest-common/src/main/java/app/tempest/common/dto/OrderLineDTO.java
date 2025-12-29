package app.tempest.common.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order line information shared across services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineDTO {
     private Long orderLineId;
     private String sku;
     private int quantity;
     private BigDecimal unitPrice;
}
