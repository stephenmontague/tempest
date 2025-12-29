package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.results.CreateShipmentResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface CreateShipmentActivity {

     @ActivityMethod
     CreateShipmentResult createShipment(CreateShipmentRequest request);
}
