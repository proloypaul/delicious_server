package com.delicious.domain.order.event;

import com.delicious.domain.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStatusChangedInternalEvent {
    private final Long orderId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
}
