package app.tempest.oms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.tempest.common.security.SecurityUtils;
import app.tempest.oms.dto.CancelOrderRequest;
import app.tempest.oms.dto.CreateOrderCommand;
import app.tempest.oms.dto.CreateOrderLineRequest;
import app.tempest.oms.dto.CreateOrderRequest;
import app.tempest.oms.dto.CreateOrderResponse;
import app.tempest.oms.dto.OrderLineCommand;
import app.tempest.oms.entity.Order;
import app.tempest.oms.entity.OrderLine;
import app.tempest.oms.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sku,
            @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        return ResponseEntity.ok(orderService.getOrders(tenantId, status, sku));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        return orderService.getOrder(id, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/lines")
    public ResponseEntity<List<OrderLine>> getOrderLines(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        return orderService.getOrderLines(id, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        String userId = SecurityUtils.getUserId(jwt).orElse(null);

        CreateOrderCommand command = toCommand(request, tenantId, userId);
        var result = orderService.createOrder(command);
        return ResponseEntity.ok(new CreateOrderResponse(
                result.getOrderId(),
                result.getStatus(),
                request.externalOrderId()));
    }

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getOrderCounts(@AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        return ResponseEntity.ok(orderService.getOrderCounts(tenantId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @RequestBody CancelOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        boolean cancelled = orderService.cancelOrder(id, tenantId, request.reason());
        return cancelled ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/workflow-status")
    public ResponseEntity<OrderService.WorkflowStatus> getWorkflowStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        return orderService.getOrderWorkflowStatus(id, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private CreateOrderCommand toCommand(CreateOrderRequest request, String tenantId, String userId) {
        List<OrderLineCommand> lines = request.lines() != null
                ? request.lines().stream()
                        .map(this::toLineCommand)
                        .toList()
                : List.of();

        return new CreateOrderCommand(
                tenantId,
                userId,
                request.externalOrderId(),
                request.customerEmail(),
                request.customerName(),
                request.shippingAddressLine1(),
                request.shippingAddressLine2(),
                request.shippingCity(),
                request.shippingState(),
                request.shippingPostalCode(),
                request.shippingCountry(),
                lines);
    }

    private OrderLineCommand toLineCommand(CreateOrderLineRequest request) {
        return new OrderLineCommand(request.sku(), request.quantity(), request.unitPrice());
    }
}
