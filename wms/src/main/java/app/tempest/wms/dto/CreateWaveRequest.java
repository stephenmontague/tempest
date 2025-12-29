package app.tempest.wms.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWaveRequest {

     @NotNull(message = "Facility ID is required")
     private Long facilityId;

     @NotEmpty(message = "At least one order ID is required")
     private List<Long> orderIds;

     private String waveNumber;
}
