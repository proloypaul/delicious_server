package com.delicious.domain.order.listener;

import com.delicious.common.config.AsyncConfig;
import com.delicious.domain.notification.service.OrderNotificationPublisher;
import com.delicious.domain.order.enums.OrderStatus;
import com.delicious.domain.order.event.OrderCreatedInternalEvent;
import com.delicious.domain.order.event.OrderStatusChangedInternalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderNotificationPublisher notificationPublisher;

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedInternalEvent event) {
        log.debug("Received OrderCreatedInternalEvent after commit for order #{}", event.getOrderId());
        notificationPublisher.publishOrderCreated(event.getOrderId());
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedInternalEvent event) {
        log.debug("Received OrderStatusChangedInternalEvent after commit for order #{}: old={}, new={}",
                event.getOrderId(), event.getOldStatus(), event.getNewStatus());

        // Notify customer when order is dispatched / on the way
        if (event.getNewStatus() == OrderStatus.ACCEPTED_BY_RIDER || event.getNewStatus() == OrderStatus.PICKED_UP) {
            notificationPublisher.publishOrderDispatched(event.getOrderId());
        }
    }
}
