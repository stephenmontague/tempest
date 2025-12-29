package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to mark an order as awaiting wave planning.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkOrderAwaitingWaveRequest {
     private Long orderId;
     private Long facilityId;
}

