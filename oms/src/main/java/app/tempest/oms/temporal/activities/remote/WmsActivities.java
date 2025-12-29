package app.tempest.oms.temporal.activities.remote;

import app.tempest.common.dto.requests.CreatePickWaveRequest;
import app.tempest.common.dto.results.CreatePickWaveResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface WmsActivities {

     @ActivityMethod
     CreatePickWaveResult createPickWave(CreatePickWaveRequest request);
}
