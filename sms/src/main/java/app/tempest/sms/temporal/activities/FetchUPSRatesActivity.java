package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for fetching shipping rates from UPS.
 */
@ActivityInterface
public interface FetchUPSRatesActivity {

    /**
     * Fetch available shipping rates from UPS.
     * 
     * @param request the fetch rates request
     * @return UPS carrier rates
     */
    @ActivityMethod
    FetchRatesResult fetchUPSRates(FetchRatesRequest request);
}

