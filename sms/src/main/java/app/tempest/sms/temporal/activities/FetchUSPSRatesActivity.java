package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for fetching shipping rates from USPS.
 */
@ActivityInterface
public interface FetchUSPSRatesActivity {

    /**
     * Fetch available shipping rates from USPS.
     * 
     * @param request the fetch rates request
     * @return USPS carrier rates
     */
    @ActivityMethod
    FetchRatesResult fetchUSPSRates(FetchRatesRequest request);
}

