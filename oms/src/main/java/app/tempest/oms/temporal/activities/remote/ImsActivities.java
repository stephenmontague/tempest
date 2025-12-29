package app.tempest.oms.temporal.activities.remote;

import app.tempest.common.dto.requests.AllocateInventoryRequest;
import app.tempest.common.dto.requests.ConsumeInventoryRequest;
import app.tempest.common.dto.requests.ReleaseInventoryRequest;
import app.tempest.common.dto.results.AllocateInventoryResult;
import app.tempest.common.dto.results.ConsumeInventoryResult;
import app.tempest.common.dto.results.ReleaseInventoryResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ImsActivities {

     @ActivityMethod
     AllocateInventoryResult allocate(AllocateInventoryRequest request);

     @ActivityMethod
     ReleaseInventoryResult releaseInventory(ReleaseInventoryRequest request);

     @ActivityMethod
     ConsumeInventoryResult consumeInventory(ConsumeInventoryRequest request);
}
