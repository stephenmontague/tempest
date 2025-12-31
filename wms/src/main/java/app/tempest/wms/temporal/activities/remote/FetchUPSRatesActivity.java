package app.tempest.wms.temporal.activities.remote;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Remote activity for fetching UPS shipping rates.
 * Executed on the SMS task queue.
 */
@ActivityInterface
public interface FetchUPSRatesActivity {

    @ActivityMethod
    FetchRatesResult fetchUPSRates(FetchRatesRequest request);
}

