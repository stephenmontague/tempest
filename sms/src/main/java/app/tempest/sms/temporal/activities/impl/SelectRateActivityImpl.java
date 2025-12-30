package app.tempest.sms.temporal.activities.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.requests.SelectRateRequest;
import app.tempest.common.dto.results.SelectRateResult;
import app.tempest.sms.entity.Shipment;
import app.tempest.sms.repository.ShipmentRepository;
import app.tempest.sms.temporal.activities.SelectRateActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SelectRateActivity.
 * Updates the shipment with the selected carrier and service level.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SelectRateActivityImpl implements SelectRateActivity {

     private final ShipmentRepository shipmentRepository;

     @Override
     @Transactional
     public SelectRateResult selectRate(SelectRateRequest request) {
          log.info("Selecting rate - tenantId: {}, shipmentId: {}, carrier: {}, serviceLevel: {}",
                    request.getTenantId(), request.getShipmentId(), 
                    request.getCarrier(), request.getServiceLevel());

          // Find the shipment
          Shipment shipment = shipmentRepository.findByIdAndTenantId(request.getShipmentId(), request.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + request.getShipmentId()));

          // Update carrier and service level
          shipment.setCarrier(request.getCarrier());
          shipment.setServiceLevel(request.getServiceLevel());
          shipmentRepository.save(shipment);

          log.info("Rate selected - shipmentId: {}, carrier: {}, serviceLevel: {}", 
                    shipment.getId(), request.getCarrier(), request.getServiceLevel());

          return SelectRateResult.builder()
                    .shipmentId(shipment.getId())
                    .carrier(request.getCarrier())
                    .serviceLevel(request.getServiceLevel())
                    .success(true)
                    .build();
     }
}

