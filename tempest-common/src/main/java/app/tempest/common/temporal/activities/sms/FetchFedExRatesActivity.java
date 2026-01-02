package app.tempest.common.temporal.activities.sms;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for fetching FedEx shipping rates.
 * SMS implements this activity on the sms-tasks queue.
 * Other services (WMS, etc.) call this activity remotely.
 * This activity is configured to fail 4 times before succeeding for demo purposes.
 */
@ActivityInterface
public interface FetchFedExRatesActivity {

    @ActivityMethod
    FetchRatesResult fetchFedExRates(FetchRatesRequest request);
}

