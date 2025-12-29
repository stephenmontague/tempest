package app.tempest.ims.temporal.activities;

import app.tempest.common.dto.requests.ReleaseInventoryRequest;
import app.tempest.common.dto.results.ReleaseInventoryResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ReleaseInventoryActivity {
     @ActivityMethod
     ReleaseInventoryResult release(ReleaseInventoryRequest releaseInventoryRequest);
}
