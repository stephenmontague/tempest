package app.tempest.common.temporal.activities.oms;

import app.tempest.common.dto.requests.ValidateOrderRequest;
import app.tempest.common.dto.results.ValidateOrderResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ValidateOrderActivity {

     @ActivityMethod
     ValidateOrderResult validate(ValidateOrderRequest request);
}
