package app.tempest.common.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a carrier shipping rate option.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierRateDTO {
     private String carrier;
     private String serviceLevel;
     private BigDecimal price;
     private String estimatedDelivery;
}

