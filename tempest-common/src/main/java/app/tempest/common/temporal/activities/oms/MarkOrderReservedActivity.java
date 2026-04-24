package app.tempest.common.temporal.activities.oms;

import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.results.MarkOrderReservedResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MarkOrderReservedActivity {

     @ActivityMethod
     MarkOrderReservedResult markReserved(MarkOrderReservedRequest request);
}
