package app.tempest.sms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.results.ConfirmShipmentResult;
import app.tempest.sms.temporal.activities.ConfirmShipmentActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConfirmShipmentActivityImpl implements ConfirmShipmentActivity {

     @Override
     public ConfirmShipmentResult confirmShipment(ConfirmShipmentRequest request) {
          log.info("Confirming shipment - shipmentId: {}, orderId: {}, shippedAt: {}",
                    request.getShipmentId(), request.getOrderId(), request.getShippedAt());

          // Stub implementation for now
          // TODO: Implement actual shipment confirmation logic
          // 1. Find shipment by ID
          // 2. Update status to SHIPPED
          // 3. Set shippedAt timestamp
          // 4. Emit shipment event

          log.info("Shipment confirmed - shipmentId: {}", request.getShipmentId());

          return ConfirmShipmentResult.builder()
                    .success(true)
                    .status("SHIPPED")
                    .build();
     }
}
