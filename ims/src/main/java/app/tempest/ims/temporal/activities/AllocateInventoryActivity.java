package app.tempest.ims.temporal.activities;

import app.tempest.common.dto.requests.AllocateInventoryRequest;
import app.tempest.common.dto.results.AllocateInventoryResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface AllocateInventoryActivity {
     @ActivityMethod
     AllocateInventoryResult allocate(AllocateInventoryRequest allocateInventoryRequest);
}
