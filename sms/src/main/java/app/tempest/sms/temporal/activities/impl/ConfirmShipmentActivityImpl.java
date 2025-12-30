package app.tempest.sms.temporal.activities.impl;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.results.ConfirmShipmentResult;
import app.tempest.sms.entity.Shipment;
import app.tempest.sms.repository.ShipmentRepository;
import app.tempest.sms.temporal.activities.ConfirmShipmentActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmShipmentActivityImpl implements ConfirmShipmentActivity {

     private final ShipmentRepository shipmentRepository;

     @Override
     @Transactional
     public ConfirmShipmentResult confirmShipment(ConfirmShipmentRequest request) {
          log.info("Confirming shipment - tenantId: {}, shipmentId: {}, orderId: {}, shippedAt: {}",
                    request.getTenantId(), request.getShipmentId(), request.getOrderId(), request.getShippedAt());

          // Find the shipment
          Shipment shipment = shipmentRepository.findByIdAndTenantId(request.getShipmentId(), request.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + request.getShipmentId()));

          // Idempotency: if already shipped, return success
          if ("SHIPPED".equals(shipment.getStatus())) {
               log.info("Shipment already confirmed - shipmentId: {}", shipment.getId());
               return ConfirmShipmentResult.builder()
                         .success(true)
                         .status("SHIPPED")
                         .build();
          }

          // Update shipment status to SHIPPED
          shipment.setStatus("SHIPPED");
          shipment.setShippedAt(request.getShippedAt() != null ? request.getShippedAt() : Instant.now());
          shipmentRepository.save(shipment);

          log.info("Shipment confirmed - shipmentId: {}, shippedAt: {}", shipment.getId(), shipment.getShippedAt());

          return ConfirmShipmentResult.builder()
                    .success(true)
                    .status("SHIPPED")
                    .build();
     }
}
