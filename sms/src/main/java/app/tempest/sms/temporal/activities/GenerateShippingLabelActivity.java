package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GenerateShippingLabelActivity {

     @ActivityMethod
     GenerateShippingLabelResult generateLabel(GenerateShippingLabelRequest request);
}
