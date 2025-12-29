package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to release allocated inventory (e.g., on cancellation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryRequest {
     private String orderId;
     private String reservationId;
     private String reason;
}

