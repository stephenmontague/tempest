package app.tempest.common.temporal.activities.oms;

import app.tempest.common.dto.requests.MarkOrderAwaitingWaveRequest;
import app.tempest.common.dto.results.MarkOrderAwaitingWaveResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MarkOrderAwaitingWaveActivity {

     @ActivityMethod
     MarkOrderAwaitingWaveResult markAwaitingWave(MarkOrderAwaitingWaveRequest request);
}
