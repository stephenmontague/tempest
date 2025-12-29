package app.tempest.oms.temporal.activities;

import app.tempest.common.dto.requests.CreateOrderRequest;
import app.tempest.common.dto.results.CreateOrderResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface CreateOrderActivity {

     @ActivityMethod
     CreateOrderResult createOrder(CreateOrderRequest request);
}
