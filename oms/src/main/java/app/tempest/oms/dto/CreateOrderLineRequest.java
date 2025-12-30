package app.tempest.oms.dto;

import java.math.BigDecimal;

public record CreateOrderLineRequest(
        String sku,
        Integer quantity,
        BigDecimal unitPrice) {
}

