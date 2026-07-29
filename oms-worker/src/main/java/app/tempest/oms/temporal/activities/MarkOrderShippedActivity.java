package app.tempest.oms.temporal.activities;

import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.results.MarkOrderShippedResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MarkOrderShippedActivity {

     @ActivityMethod
     MarkOrderShippedResult markShipped(MarkOrderShippedRequest request);
}
