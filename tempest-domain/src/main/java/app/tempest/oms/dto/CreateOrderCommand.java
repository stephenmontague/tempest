package app.tempest.oms.dto;

import java.util.List;

public record CreateOrderCommand(
        String tenantId,
        String userId,
        String externalOrderId,
        String customerEmail,
        String customerName,
        String shippingAddressLine1,
        String shippingAddressLine2,
        String shippingCity,
        String shippingState,
        String shippingPostalCode,
        String shippingCountry,
        List<OrderLineCommand> lines) {
}

