package app.tempest.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shipping address information shared across services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipToDTO {
     private String name;
     private String addressLine1;
     private String addressLine2;
     private String city;
     private String state;
     private String postalCode;
     private String country;
}
