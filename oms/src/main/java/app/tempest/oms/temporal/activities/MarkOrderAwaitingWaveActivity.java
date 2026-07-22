package app.tempest.oms.temporal.activities;

import app.tempest.common.dto.requests.MarkOrderAwaitingWaveRequest;
import app.tempest.common.dto.results.MarkOrderAwaitingWaveResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MarkOrderAwaitingWaveActivity {

     @ActivityMethod
     MarkOrderAwaitingWaveResult markAwaitingWave(MarkOrderAwaitingWaveRequest request);
}
