package app.tempest.common.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of marking an order as reserved.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkOrderReservedResult {
     private boolean success;
     private String previousStatus;
     private String currentStatus;
}

