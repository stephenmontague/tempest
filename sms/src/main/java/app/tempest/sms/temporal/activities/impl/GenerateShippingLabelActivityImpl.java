package app.tempest.sms.temporal.activities.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import app.tempest.sms.entity.Shipment;
import app.tempest.sms.repository.ShipmentRepository;
import app.tempest.sms.temporal.activities.GenerateShippingLabelActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateShippingLabelActivityImpl implements GenerateShippingLabelActivity {

     private final ShipmentRepository shipmentRepository;

     @Override
     @Transactional
     public GenerateShippingLabelResult generateLabel(GenerateShippingLabelRequest request) {
          log.info("Generating shipping label - tenantId: {}, shipmentId: {}, orderId: {}, carrier: {}",
                    request.getTenantId(), request.getShipmentId(), request.getOrderId(), request.getCarrier());

          // Find the shipment
          Shipment shipment = shipmentRepository.findByIdAndTenantId(request.getShipmentId(), request.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + request.getShipmentId()));

          // Idempotency: if label already generated, return existing data
          if (shipment.getTrackingNumber() != null && shipment.getLabelUrl() != null) {
               log.info("Label already exists - shipmentId: {}, trackingNumber: {}", 
                         shipment.getId(), shipment.getTrackingNumber());
               return GenerateShippingLabelResult.builder()
                         .labelId(shipment.getId())
                         .trackingNumber(shipment.getTrackingNumber())
                         .labelUrl(shipment.getLabelUrl())
                         .build();
          }

          // Generate fake tracking number based on carrier
          String carrierPrefix = switch (shipment.getCarrier().toUpperCase()) {
               case "UPS" -> "1Z";
               case "FEDEX" -> "FX";
               case "USPS" -> "94";
               default -> "TRK";
          };
          String trackingNumber = carrierPrefix + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
          String labelUrl = "https://labels.tempest.example.com/shipment/" + shipment.getId() + "/label.pdf";

          // Update shipment with label info
          shipment.setTrackingNumber(trackingNumber);
          shipment.setLabelUrl(labelUrl);
          shipment.setStatus("LABEL_GENERATED");
          shipmentRepository.save(shipment);

          log.info("Label generated - shipmentId: {}, trackingNumber: {}, labelUrl: {}", 
                    shipment.getId(), trackingNumber, labelUrl);

          return GenerateShippingLabelResult.builder()
                    .labelId(shipment.getId())
                    .trackingNumber(trackingNumber)
                    .labelUrl(labelUrl)
                    .build();
     }
}
