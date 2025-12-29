package app.tempest.oms.temporal.activities.remote;

import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.results.ConfirmShipmentResult;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SmsActivities {

     @ActivityMethod
     CreateShipmentResult createShipment(CreateShipmentRequest request);

     @ActivityMethod
     GenerateShippingLabelResult generateLabel(GenerateShippingLabelRequest request);

     @ActivityMethod
     ConfirmShipmentResult confirmShipment(ConfirmShipmentRequest request);
}
