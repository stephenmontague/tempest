package app.tempest.sms.temporal.activities.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import app.tempest.sms.temporal.activities.GenerateShippingLabelActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GenerateShippingLabelActivityImpl implements GenerateShippingLabelActivity {

     @Override
     public GenerateShippingLabelResult generateLabel(GenerateShippingLabelRequest request) {
          log.info("Generating shipping label - shipmentId: {}, orderId: {}, carrier: {}",
                    request.getShipmentId(), request.getOrderId(), request.getCarrier());

          // Stub implementation for now
          // TODO: Implement actual label generation logic
          // 1. Call carrier API to generate label
          // 2. Store label in blob storage
          // 3. Create Label entity with tracking number
          // 4. Return label details

          // Simulate label generation
          Long labelId = System.currentTimeMillis();
          String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
          String labelUrl = "https://labels.example.com/" + labelId + ".pdf";

          log.info("Label generated - labelId: {}, trackingNumber: {}", labelId, trackingNumber);

          return GenerateShippingLabelResult.builder()
                    .labelId(labelId)
                    .trackingNumber(trackingNumber)
                    .labelUrl(labelUrl)
                    .build();
     }
}
