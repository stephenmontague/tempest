package app.tempest.wms.temporal.activities.remote;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Remote activity for fetching FedEx shipping rates.
 * Executed on the SMS task queue.
 * This activity is configured to fail 4 times before succeeding for demo purposes.
 */
@ActivityInterface
public interface FetchFedExRatesActivity {

    @ActivityMethod
    FetchRatesResult fetchFedExRates(FetchRatesRequest request);
}

