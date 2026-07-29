package app.tempest.sms.temporal.activities.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.tempest.common.dto.CarrierRateDTO;
import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.requests.SelectRateRequest;
import app.tempest.common.dto.results.ConfirmShipmentResult;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.common.dto.results.FetchRatesResult;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import app.tempest.common.dto.results.SelectRateResult;
import app.tempest.common.temporal.activities.sms.SmsActivities;
import app.tempest.sms.entity.Shipment;
import app.tempest.sms.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SmsActivities for remote calls from other services.
 * This is registered on the sms-tasks queue and handles cross-service activity
 * calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsActivitiesImpl implements SmsActivities {

        private final ShipmentRepository shipmentRepository;

        @Override
        @Transactional
        public CreateShipmentResult createShipment(CreateShipmentRequest request) {
                log.info("Creating shipment - tenantId: {}, orderId: {}, carrier: {}, serviceLevel: {}",
                                request.getTenantId(), request.getOrderId(), request.getCarrier(),
                                request.getServiceLevel());

                // Idempotency check: return existing shipment if one exists for this order
                List<Shipment> existingShipments = shipmentRepository.findByTenantIdAndOrderId(
                                request.getTenantId(), request.getOrderId());

                if (!existingShipments.isEmpty()) {
                        Shipment existing = existingShipments.get(0);
                        log.info("Shipment already exists - shipmentId: {}, orderId: {}", existing.getId(),
                                        request.getOrderId());
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

        @Override
        @Transactional
        public GenerateShippingLabelResult generateLabel(GenerateShippingLabelRequest request) {
                log.info("Generating shipping label - tenantId: {}, shipmentId: {}, orderId: {}, carrier: {}",
                                request.getTenantId(), request.getShipmentId(), request.getOrderId(),
                                request.getCarrier());

                // Find the shipment
                Shipment shipment = shipmentRepository
                                .findByIdAndTenantId(request.getShipmentId(), request.getTenantId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Shipment not found: " + request.getShipmentId()));

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

        @Override
        @Transactional
        public ConfirmShipmentResult confirmShipment(ConfirmShipmentRequest request) {
                log.info("Confirming shipment - tenantId: {}, shipmentId: {}, orderId: {}, shippedAt: {}",
                                request.getTenantId(), request.getShipmentId(), request.getOrderId(),
                                request.getShippedAt());

                // Find the shipment
                Shipment shipment = shipmentRepository
                                .findByIdAndTenantId(request.getShipmentId(), request.getTenantId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Shipment not found: " + request.getShipmentId()));

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

                log.info("Shipment confirmed - shipmentId: {}, shippedAt: {}", shipment.getId(),
                                shipment.getShippedAt());

                return ConfirmShipmentResult.builder()
                                .success(true)
                                .status("SHIPPED")
                                .build();
        }

        @Override
        public FetchRatesResult fetchRates(FetchRatesRequest request) {
                log.info("Fetching shipping rates - tenantId: {}, shipmentId: {}, orderId: {}",
                                request.getTenantId(), request.getShipmentId(), request.getOrderId());

                // Fake carrier rates - in production this would call carrier APIs
                List<CarrierRateDTO> rates = List.of(
                                CarrierRateDTO.builder()
                                                .carrier("USPS")
                                                .serviceLevel("Priority")
                                                .price(new BigDecimal("6.25"))
                                                .estimatedDelivery("2-3 business days")
                                                .build(),
                                CarrierRateDTO.builder()
                                                .carrier("UPS")
                                                .serviceLevel("Ground")
                                                .price(new BigDecimal("8.50"))
                                                .estimatedDelivery("3-5 business days")
                                                .build(),
                                CarrierRateDTO.builder()
                                                .carrier("UPS")
                                                .serviceLevel("2nd Day Air")
                                                .price(new BigDecimal("18.75"))
                                                .estimatedDelivery("2 business days")
                                                .build(),
                                CarrierRateDTO.builder()
                                                .carrier("FedEx")
                                                .serviceLevel("Express")
                                                .price(new BigDecimal("15.00"))
                                                .estimatedDelivery("1-2 business days")
                                                .build(),
                                CarrierRateDTO.builder()
                                                .carrier("FedEx")
                                                .serviceLevel("Ground")
                                                .price(new BigDecimal("7.99"))
                                                .estimatedDelivery("4-6 business days")
                                                .build());

                log.info("Fetched {} rates for shipmentId: {}", rates.size(), request.getShipmentId());

                return FetchRatesResult.builder()
                                .shipmentId(request.getShipmentId())
                                .rates(rates)
                                .build();
        }

        @Override
        @Transactional
        public SelectRateResult selectRate(SelectRateRequest request) {
                log.info("Selecting rate - tenantId: {}, shipmentId: {}, carrier: {}, serviceLevel: {}",
                                request.getTenantId(), request.getShipmentId(),
                                request.getCarrier(), request.getServiceLevel());

                // Find the shipment
                Shipment shipment = shipmentRepository
                                .findByIdAndTenantId(request.getShipmentId(), request.getTenantId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Shipment not found: " + request.getShipmentId()));

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
