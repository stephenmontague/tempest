package app.tempest.common.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parcel information for shipping.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelDTO {
     private BigDecimal weightLbs;
     private BigDecimal lengthIn;
     private BigDecimal widthIn;
     private BigDecimal heightIn;
}
