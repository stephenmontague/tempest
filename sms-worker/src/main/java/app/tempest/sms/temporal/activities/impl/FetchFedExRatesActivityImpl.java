package app.tempest.sms.temporal.activities.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.CarrierRateDTO;
import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import app.tempest.common.temporal.activities.sms.FetchFedExRatesActivity;
import io.temporal.activity.Activity;
import io.temporal.failure.ApplicationFailure;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of FetchFedExRatesActivity.
 * This activity simulates failures for demo purposes - it fails 4 times
 * before succeeding on the 5th attempt to demonstrate Temporal's retry capabilities.
 */
@Slf4j
@Component
public class FetchFedExRatesActivityImpl implements FetchFedExRatesActivity {

    private static final int SUCCEED_ON_ATTEMPT = 5;

    @Override
    public FetchRatesResult fetchFedExRates(FetchRatesRequest request) {
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        log.info("Fetching FedEx rates - attempt: {}, tenantId: {}, shipmentId: {}, orderId: {}",
                attempt, request.getTenantId(), request.getShipmentId(), request.getOrderId());

        // Simulate API call delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Fail on attempts 1-4, succeed on attempt 5
        if (attempt < SUCCEED_ON_ATTEMPT) {
            String errorMessage = String.format(
                    "FedEx API temporarily unavailable (attempt %d of %d) - shipmentId: %d",
                    attempt, SUCCEED_ON_ATTEMPT, request.getShipmentId());
            log.warn(errorMessage);
            throw ApplicationFailure.newFailure(
                    errorMessage,
                    "FedExTemporaryError");
        }

        log.info("FedEx rates fetched successfully on attempt {} - shipmentId: {}",
                attempt, request.getShipmentId());

        List<CarrierRateDTO> rates = List.of(
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

        return FetchRatesResult.builder()
                .shipmentId(request.getShipmentId())
                .rates(rates)
                .build();
    }
}

