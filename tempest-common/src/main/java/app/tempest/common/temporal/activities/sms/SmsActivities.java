package app.tempest.common.temporal.activities.sms;

import app.tempest.common.dto.requests.ConfirmShipmentRequest;
import app.tempest.common.dto.requests.CreateShipmentRequest;
import app.tempest.common.dto.requests.FetchRatesRequest;
import app.tempest.common.dto.requests.GenerateShippingLabelRequest;
import app.tempest.common.dto.requests.SelectRateRequest;
import app.tempest.common.dto.results.ConfirmShipmentResult;
import app.tempest.common.dto.results.CreateShipmentResult;
import app.tempest.common.dto.results.FetchRatesResult;
import app.tempest.common.dto.results.GenerateShippingLabelResult;
import app.tempest.common.dto.results.SelectRateResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * SMS Activities interface - shared between services.
 * SMS implements these activities on the sms-tasks queue.
 * Other services (OMS, WMS, etc.) call these activities remotely.
 */
@ActivityInterface
public interface SmsActivities {

    @ActivityMethod
    CreateShipmentResult createShipment(CreateShipmentRequest request);

    @ActivityMethod
    GenerateShippingLabelResult generateLabel(GenerateShippingLabelRequest request);

    @ActivityMethod
    ConfirmShipmentResult confirmShipment(ConfirmShipmentRequest request);

    @ActivityMethod
    FetchRatesResult fetchRates(FetchRatesRequest request);

    @ActivityMethod
    SelectRateResult selectRate(SelectRateRequest request);
}

