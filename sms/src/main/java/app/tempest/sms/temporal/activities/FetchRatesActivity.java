package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for fetching shipping rates from carriers.
 */
@ActivityInterface
public interface FetchRatesActivity {

     /**
      * Fetch available shipping rates for a shipment.
      * This calls external carrier APIs to get rate quotes.
      * 
      * @param request the fetch rates request
      * @return list of available carrier rates
      */
     @ActivityMethod
     FetchRatesResult fetchRates(FetchRatesRequest request);
}

