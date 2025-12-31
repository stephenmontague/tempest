package app.tempest.sms.temporal.activities.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.CarrierRateDTO;
import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import app.tempest.sms.temporal.activities.FetchUSPSRatesActivity;
import io.temporal.activity.Activity;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of FetchUSPSRatesActivity.
 * Returns USPS shipping rates for demo purposes.
 */
@Slf4j
@Component
public class FetchUSPSRatesActivityImpl implements FetchUSPSRatesActivity {

    @Override
    public FetchRatesResult fetchUSPSRates(FetchRatesRequest request) {
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        log.info("Fetching USPS rates - attempt: {}, tenantId: {}, shipmentId: {}, orderId: {}",
                attempt, request.getTenantId(), request.getShipmentId(), request.getOrderId());

        // Simulate API call delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<CarrierRateDTO> rates = List.of(
                CarrierRateDTO.builder()
                        .carrier("USPS")
                        .serviceLevel("Priority")
                        .price(new BigDecimal("6.25"))
                        .estimatedDelivery("2-3 business days")
                        .build()
        );

        log.info("USPS rates fetched successfully - shipmentId: {}", request.getShipmentId());

        return FetchRatesResult.builder()
                .shipmentId(request.getShipmentId())
                .rates(rates)
                .build();
    }
}

