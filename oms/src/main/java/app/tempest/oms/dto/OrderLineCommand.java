package app.tempest.oms.dto;

import java.math.BigDecimal;

/**
 * Command object for an order line.
 */
public record OrderLineCommand(
        String sku,
        Integer quantity,
        BigDecimal unitPrice) {
}

