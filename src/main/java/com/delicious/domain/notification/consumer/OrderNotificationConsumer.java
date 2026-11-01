package com.delicious.domain.notification.consumer;

import com.delicious.domain.notification.dto.OrderCreatedNotificationEvent;
import com.delicious.domain.notification.dto.OrderDispatchedNotificationEvent;
import com.delicious.domain.notification.dto.SellerOrderGroupDto;
import com.delicious.domain.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationConsumer {

    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    private static final String DEDUP_KEY_PREFIX = "notification:dedup:";
    private static final Duration DEDUP_EXPIRATION = Duration.ofHours(24);

    @RabbitListener(queues = "${notification.rabbitmq.queues.seller-order-created:order.seller.notification.queue}")
    public void consumeSellerOrderCreated(OrderCreatedNotificationEvent event) {
        log.info("Received OrderCreatedNotificationEvent from RabbitMQ for order #{}", event.getOrderId());

        if (event.getSellerGroups() == null || event.getSellerGroups().isEmpty()) {
            log.warn("No seller groups found for order #{}", event.getOrderId());
            return;
        }

        for (SellerOrderGroupDto sellerGroup : event.getSellerGroups()) {
            String dedupKey = DEDUP_KEY_PREFIX + "seller:" + event.getOrderId() + ":" + sellerGroup.getSellerId();
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(dedupKey, "PROCESSED", DEDUP_EXPIRATION);

            if (Boolean.FALSE.equals(isNew)) {
                log.info("Skipping duplicate notification for order #{} seller #{}", event.getOrderId(), sellerGroup.getSellerId());
                continue;
            }

            emailService.sendSellerNewOrderAlert(sellerGroup, event);
        }
    }

    @RabbitListener(queues = "${notification.rabbitmq.queues.customer-order-dispatched:order.customer.notification.queue}")
    public void consumeCustomerOrderDispatched(OrderDispatchedNotificationEvent event) {
        log.info("Received OrderDispatchedNotificationEvent from RabbitMQ for order #{}, status={}",
                event.getOrderId(), event.getNewStatus());

        String dedupKey = DEDUP_KEY_PREFIX + "customer:" + event.getOrderId() + ":" + event.getNewStatus();
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(dedupKey, "PROCESSED", DEDUP_EXPIRATION);

        if (Boolean.FALSE.equals(isNew)) {
            log.info("Skipping duplicate customer dispatch notification for order #{} status {}",
                    event.getOrderId(), event.getNewStatus());
            return;
        }

        emailService.sendCustomerOrderOnTheWay(event);
    }
}
