package app.tempest.sms.temporal.activities;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for fetching shipping rates from FedEx.
 * This activity simulates failures for demo purposes.
 */
@ActivityInterface
public interface FetchFedExRatesActivity {

    /**
     * Fetch available shipping rates from FedEx.
     * Note: This activity is configured to fail 4 times before succeeding
     * on the 5th attempt for demo purposes.
     * 
     * @param request the fetch rates request
     * @return FedEx carrier rates
     */
    @ActivityMethod
    FetchRatesResult fetchFedExRates(FetchRatesRequest request);
}

