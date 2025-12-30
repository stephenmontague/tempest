package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.SelectRateRequest;
import app.tempest.common.dto.results.SelectRateResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for selecting a shipping rate for a shipment.
 */
@ActivityInterface
public interface SelectRateActivity {

     /**
      * Select a carrier rate for a shipment.
      * Updates the shipment with the selected carrier and service level.
      * 
      * @param request the select rate request
      * @return result of the selection
      */
     @ActivityMethod
     SelectRateResult selectRate(SelectRateRequest request);
}

