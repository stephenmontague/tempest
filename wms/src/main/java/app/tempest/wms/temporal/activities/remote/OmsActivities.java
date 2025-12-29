package app.tempest.wms.temporal.activities.remote;

import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.results.MarkOrderReservedResult;
import app.tempest.common.dto.results.MarkOrderShippedResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Remote activities for OMS (Order Management Service).
 * These activities are executed on the OMS task queue.
 */
@ActivityInterface
public interface OmsActivities {

     @ActivityMethod
     MarkOrderReservedResult markOrderReserved(MarkOrderReservedRequest request);

     @ActivityMethod
     MarkOrderShippedResult markOrderShipped(MarkOrderShippedRequest request);
}
