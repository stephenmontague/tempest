package app.tempest.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tracking shipment state within a wave workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStateDTO {
     private Long shipmentId;
     private Long orderId;
     private String status;  // CREATED, RATE_SELECTED, LABEL_GENERATED, SHIPPED
     private String carrier;
     private String serviceLevel;
     private String trackingNumber;
     private String labelUrl;
}

