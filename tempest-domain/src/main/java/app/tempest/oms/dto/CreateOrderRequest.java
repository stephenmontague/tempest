package app.tempest.oms.dto;

import java.util.List;

public record CreateOrderRequest(
        String externalOrderId,
        String customerEmail,
        String customerName,
        String shippingAddressLine1,
        String shippingAddressLine2,
        String shippingCity,
        String shippingState,
        String shippingPostalCode,
        String shippingCountry,
        List<CreateOrderLineRequest> lines) {
}

