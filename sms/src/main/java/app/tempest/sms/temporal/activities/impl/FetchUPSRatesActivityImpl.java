package app.tempest.sms.temporal.activities.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.CarrierRateDTO;
import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import app.tempest.common.temporal.activities.sms.FetchUPSRatesActivity;
import io.temporal.activity.Activity;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of FetchUPSRatesActivity.
 * Returns UPS shipping rates for demo purposes.
 */
@Slf4j
@Component
public class FetchUPSRatesActivityImpl implements FetchUPSRatesActivity {

    @Override
    public FetchRatesResult fetchUPSRates(FetchRatesRequest request) {
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        log.info("Fetching UPS rates - attempt: {}, tenantId: {}, shipmentId: {}, orderId: {}",
                attempt, request.getTenantId(), request.getShipmentId(), request.getOrderId());

        // Simulate API call delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<CarrierRateDTO> rates = List.of(
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
                        .build()
        );

        log.info("UPS rates fetched successfully - shipmentId: {}", request.getShipmentId());

        return FetchRatesResult.builder()
                .shipmentId(request.getShipmentId())
                .rates(rates)
                .build();
    }
}

