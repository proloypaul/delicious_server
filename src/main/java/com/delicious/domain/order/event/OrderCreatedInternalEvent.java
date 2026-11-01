package com.delicious.domain.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreatedInternalEvent {
    private final Long orderId;
    private final Long customerId;
}
