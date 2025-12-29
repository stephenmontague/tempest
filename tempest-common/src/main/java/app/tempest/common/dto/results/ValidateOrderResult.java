package app.tempest.common.dto.results;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of order validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOrderResult {
     private boolean valid;
     private List<String> errors;
}

