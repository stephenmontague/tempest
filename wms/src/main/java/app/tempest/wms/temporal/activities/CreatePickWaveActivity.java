package app.tempest.wms.temporal.activities;

import app.tempest.common.dto.requests.CreatePickWaveRequest;
import app.tempest.common.dto.results.CreatePickWaveResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface CreatePickWaveActivity {

     @ActivityMethod
     CreatePickWaveResult createPickWave(CreatePickWaveRequest request);
}
