package app.tempest.common.dto.results;

import java.util.List;

import app.tempest.common.dto.CarrierRateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of fetching shipping rates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchRatesResult {
     private Long shipmentId;
     private List<CarrierRateDTO> rates;
}
