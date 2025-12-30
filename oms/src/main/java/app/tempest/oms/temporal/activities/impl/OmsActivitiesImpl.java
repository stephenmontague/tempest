package app.tempest.oms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.dto.requests.MarkOrderReservedRequest;
import app.tempest.common.dto.requests.MarkOrderShippedRequest;
import app.tempest.common.dto.results.MarkOrderReservedResult;
import app.tempest.common.dto.results.MarkOrderShippedResult;
import app.tempest.common.temporal.activities.OmsActivities;
import app.tempest.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of OmsActivities for remote calls from other services.
 * This is registered on the oms-tasks queue and handles cross-service activity calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OmsActivitiesImpl implements OmsActivities {

    private final OrderService orderService;

    @Override
    public MarkOrderReservedResult markOrderReserved(MarkOrderReservedRequest request) {
        log.info("Marking order as RESERVED - orderId: {}, reservationId: {}", 
                request.getOrderId(), request.getReservationId());

        // Update order status to RESERVED
        orderService.updateOrderStatus(request.getOrderId(), "RESERVED");
        
        log.info("Order marked as RESERVED - orderId: {}", request.getOrderId());
        
        return MarkOrderReservedResult.builder()
                .success(true)
                .previousStatus("AWAITING_WAVE")
                .currentStatus("RESERVED")
                .build();
    }

    @Override
    public MarkOrderShippedResult markOrderShipped(MarkOrderShippedRequest request) {
        log.info("Marking order as SHIPPED - orderId: {}, shipmentId: {}, trackingNumber: {}", 
                request.getOrderId(), request.getShipmentId(), request.getTrackingNumber());

        // Update order status to SHIPPED
        orderService.updateOrderStatus(request.getOrderId(), "SHIPPED");
        
        log.info("Order marked as SHIPPED - orderId: {}", request.getOrderId());
        
        return MarkOrderShippedResult.builder()
                .success(true)
                .previousStatus("RESERVED")
                .currentStatus("SHIPPED")
                .build();
    }
}

