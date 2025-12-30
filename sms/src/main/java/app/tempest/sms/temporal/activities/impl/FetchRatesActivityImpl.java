package app.tempest.sms.temporal.activities.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.CarrierRateDTO;
import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import app.tempest.sms.temporal.activities.FetchRatesActivity;
import lombok.extern.slf4j.Slf4j;

/**
 * Fake implementation of FetchRatesActivity.
 * Returns hardcoded carrier rates for demo purposes.
 * In production, this would call actual carrier APIs.
 */
@Slf4j
@Component
public class FetchRatesActivityImpl implements FetchRatesActivity {

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
                              .build()
          );

          log.info("Fetched {} rates for shipmentId: {}", rates.size(), request.getShipmentId());

          return FetchRatesResult.builder()
                    .shipmentId(request.getShipmentId())
                    .rates(rates)
                    .build();
     }
}

