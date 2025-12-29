package app.tempest.ims.temporal.activities;

import app.tempest.common.dto.requests.ConsumeInventoryRequest;
import app.tempest.common.dto.results.ConsumeInventoryResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ConsumeInventoryActivity {
     @ActivityMethod
     ConsumeInventoryResult consume(ConsumeInventoryRequest consumeInventoryRequest);
}
