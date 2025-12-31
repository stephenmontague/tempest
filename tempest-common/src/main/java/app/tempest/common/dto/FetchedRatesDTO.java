package app.tempest.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing fetched rates for a shipment, including status of each carrier fetch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchedRatesDTO {
    private Long shipmentId;
    private String status; // PENDING, FETCHING, COMPLETED, FAILED
    private List<CarrierRateDTO> rates;
    private String uspsStatus; // PENDING, FETCHING, COMPLETED, FAILED
    private String upsStatus;
    private String fedexStatus;
    private String errorMessage;
}

