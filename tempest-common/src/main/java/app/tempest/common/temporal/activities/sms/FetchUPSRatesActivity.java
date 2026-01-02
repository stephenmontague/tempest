package app.tempest.common.temporal.activities.sms;

import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.results.FetchRatesResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity for fetching UPS shipping rates.
 * SMS implements this activity on the sms-tasks queue.
 * Other services (WMS, etc.) call this activity remotely.
 */
@ActivityInterface
public interface FetchUPSRatesActivity {

    @ActivityMethod
    FetchRatesResult fetchUPSRates(FetchRatesRequest request);
}

