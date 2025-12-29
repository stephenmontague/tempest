package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.results.ConfirmShipmentResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ConfirmShipmentActivity {

     @ActivityMethod
     ConfirmShipmentResult confirmShipment(ConfirmShipmentRequest request);
}
