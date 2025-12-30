package app.tempest.wms.temporal.activities;

import app.tempest.common.dto.requests.UpdateWaveStatusRequest;
import app.tempest.common.dto.results.UpdateWaveStatusResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for updating wave status in the database.
 * This is called by the workflow to sync the wave entity status
 * with the workflow state.
 */
@ActivityInterface
public interface UpdateWaveStatusActivity {

     /**
      * Update the wave status in the database.
      * 
      * @param request the update request
      * @return the result of the update
      */
     @ActivityMethod
     UpdateWaveStatusResult updateStatus(UpdateWaveStatusRequest request);
}

