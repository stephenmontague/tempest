package app.tempest.oms.dto;

public record CreateOrderResponse(Long orderId, String status, String externalOrderId) {
}
