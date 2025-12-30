package app.tempest.sms.temporal.activities.impl;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.sms.entity.Shipment;
import app.tempest.sms.repository.ShipmentRepository;
import app.tempest.sms.temporal.activities.CreateShipmentActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateShipmentActivityImpl implements CreateShipmentActivity {

     private final ShipmentRepository shipmentRepository;

     @Override
     @Transactional
     public CreateShipmentResult createShipment(CreateShipmentRequest request) {
          log.info("Creating shipment - tenantId: {}, orderId: {}, carrier: {}, serviceLevel: {}",
                    request.getTenantId(), request.getOrderId(), request.getCarrier(), request.getServiceLevel());

          // Idempotency check: return existing shipment if one exists for this order
          List<Shipment> existingShipments = shipmentRepository.findByTenantIdAndOrderId(
                    request.getTenantId(), request.getOrderId());
          
          if (!existingShipments.isEmpty()) {
               Shipment existing = existingShipments.get(0);
               log.info("Shipment already exists - shipmentId: {}, orderId: {}", existing.getId(), request.getOrderId());
               return CreateShipmentResult.builder()
                         .shipmentId(existing.getId())
                         .status(existing.getStatus())
                         .alreadyExisted(true)
                         .build();
          }

          // Create new shipment
          Shipment shipment = Shipment.builder()
                    .orderId(request.getOrderId())
                    .facilityId(request.getFacilityId())
                    .carrier(request.getCarrier())
                    .serviceLevel(request.getServiceLevel())
                    .status("CREATED")
                    .build();
          shipment.setTenantId(request.getTenantId());

          Shipment saved = shipmentRepository.save(shipment);
          log.info("Shipment created - shipmentId: {}, orderId: {}, tenantId: {}", 
                    saved.getId(), request.getOrderId(), request.getTenantId());

          return CreateShipmentResult.builder()
                    .shipmentId(saved.getId())
                    .status(saved.getStatus())
                    .alreadyExisted(false)
                    .build();
     }
}
