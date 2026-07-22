package app.tempest.oms.temporal.activities.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.ValidateOrderRequest;
import app.tempest.common.dto.results.ValidateOrderResult;
import app.tempest.oms.temporal.activities.ValidateOrderActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ValidateOrderActivityImpl implements ValidateOrderActivity {

     @Override
     public ValidateOrderResult validate(ValidateOrderRequest request) {
          log.info("Validating order - requestId: {}, externalOrderId: {}",
                    request.getRequestId(), request.getExternalOrderId());

          List<String> errors = new ArrayList<>();

          // Basic validation
          if (request.getOrderLines() == null || request.getOrderLines().isEmpty()) {
               errors.add("Order must have at least one line item");
          }

          if (request.getShipTo() == null) {
               errors.add("Shipping address is required");
          }

          boolean valid = errors.isEmpty();
          log.info("Order validation {} - requestId: {}", valid ? "passed" : "failed", request.getRequestId());

          return ValidateOrderResult.builder()
                    .valid(valid)
                    .errors(errors)
                    .build();
     }
}
