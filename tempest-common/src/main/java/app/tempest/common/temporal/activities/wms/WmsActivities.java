package app.tempest.common.temporal.activities.wms;

import app.tempest.common.dto.requests.CreatePickWaveRequest;
import app.tempest.common.dto.results.CreatePickWaveResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * WMS Activities interface - shared between services.
 * WMS implements these activities on the wms-tasks queue.
 * Other services (OMS, etc.) call these activities remotely.
 */
@ActivityInterface
public interface WmsActivities {

    @ActivityMethod
    CreatePickWaveResult createPickWave(CreatePickWaveRequest request);
}

