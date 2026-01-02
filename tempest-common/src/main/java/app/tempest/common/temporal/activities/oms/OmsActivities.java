package app.tempest.common.temporal.activities.oms;

import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.results.MarkOrderReservedResult;
import app.tempest.common.dto.results.MarkOrderShippedResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * OMS Activities interface - shared between services.
 * OMS implements these activities on the oms-tasks queue.
 * Other services (WMS, etc.) call these activities remotely.
 */
@ActivityInterface
public interface OmsActivities {

    @ActivityMethod
    MarkOrderReservedResult markOrderReserved(MarkOrderReservedRequest request);

    @ActivityMethod
    MarkOrderShippedResult markOrderShipped(MarkOrderShippedRequest request);
}

