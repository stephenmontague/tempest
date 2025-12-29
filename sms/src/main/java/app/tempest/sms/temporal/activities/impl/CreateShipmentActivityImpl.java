package app.tempest.sms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.sms.temporal.activities.CreateShipmentActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateShipmentActivityImpl implements CreateShipmentActivity {

     @Override
     public CreateShipmentResult createShipment(CreateShipmentRequest request) {
          log.info("Creating shipment - orderId: {}, carrier: {}, serviceLevel: {}",
                    request.getOrderId(), request.getCarrier(), request.getServiceLevel());

          // Stub implementation for now
          // TODO: Implement actual shipment creation logic
          // 1. Check if shipment for orderId already exists (idempotency)
          // 2. If exists, return existing shipment
          // 3. Create Shipment entity with status CREATED
          // 4. Return shipment details

          // Simulate shipment ID generation
          Long shipmentId = System.currentTimeMillis();

          log.info("Shipment created - shipmentId: {}, orderId: {}", shipmentId, request.getOrderId());

          return CreateShipmentResult.builder()
                    .shipmentId(shipmentId)
                    .status("CREATED")
                    .alreadyExisted(false)
                    .build();
     }
}
