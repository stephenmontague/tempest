package app.tempest.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to mark an order as reserved (inventory allocated).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkOrderReservedRequest {
     private Long orderId;
     private String reservationId;
}

