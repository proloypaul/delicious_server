package com.delicious.domain.notification.service;

import com.delicious.domain.customer.dto.CustomerProfileResponse;
import com.delicious.domain.customer.service.CustomerService;
import com.delicious.domain.notification.dto.OrderItemDetailDto;
import com.delicious.domain.notification.dto.OrderCreatedNotificationEvent;
import com.delicious.domain.notification.dto.OrderDispatchedNotificationEvent;
import com.delicious.domain.notification.dto.SellerOrderGroupDto;
import com.delicious.domain.order.entity.Order;
import com.delicious.domain.order.entity.OrderItem;
import com.delicious.domain.order.repository.OrderRepository;
import com.delicious.domain.product.entity.Product;
import com.delicious.domain.product.repository.ProductRepository;
import com.delicious.domain.rider.dto.RiderResponse;
import com.delicious.domain.rider.service.RiderService;
import com.delicious.domain.seller.dto.SellerProfileResponse;
import com.delicious.domain.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final SellerService sellerService;
    private final RiderService riderService;

    @Value("${notification.rabbitmq.exchange:delicious.notification.exchange}")
    private String exchangeName;

    @Value("${notification.rabbitmq.routing-keys.order-created:order.created}")
    private String orderCreatedRoutingKey;

    @Value("${notification.rabbitmq.routing-keys.order-dispatched:order.dispatched}")
    private String orderDispatchedRoutingKey;

    public void publishOrderCreated(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("Cannot publish OrderCreated event; order #{} not found.", orderId);
                return;
            }

            CustomerProfileResponse customerProfile = null;
            try {
                customerProfile = customerService.getProfile(order.getCustomerId());
            } catch (Exception ex) {
                log.warn("Could not retrieve customer #{} profile for notification: {}", order.getCustomerId(), ex.getMessage());
            }

            // Fetch products to map seller IDs and product names
            List<Long> productIds = order.getItems().stream()
                    .map(OrderItem::getProductId)
                    .distinct()
                    .toList();

            List<Product> products = productRepository.findAllById(productIds);
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            List<Long> sellerIds = products.stream()
                    .map(Product::getSellerId)
                    .distinct()
                    .toList();

            Map<Long, SellerProfileResponse> sellerMap = Collections.emptyMap();
            try {
                sellerMap = sellerService.getSellerProfilesByUserIds(sellerIds);
            } catch (Exception ex) {
                log.warn("Could not retrieve seller profiles for notification: {}", ex.getMessage());
            }

            // Group order items by seller
            Map<Long, List<OrderItemDetailDto>> sellerItemsMap = new HashMap<>();
            for (OrderItem item : order.getItems()) {
                Product product = productMap.get(item.getProductId());
                if (product == null) continue;

                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                OrderItemDetailDto detail = OrderItemDetailDto.builder()
                        .productId(product.getId())
                        .foodName(product.getFoodName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getPrice())
                        .totalPrice(itemTotal)
                        .build();

                sellerItemsMap.computeIfAbsent(product.getSellerId(), k -> new ArrayList<>()).add(detail);
            }

            List<SellerOrderGroupDto> sellerGroups = new ArrayList<>();
            for (Map.Entry<Long, List<OrderItemDetailDto>> entry : sellerItemsMap.entrySet()) {
                Long sId = entry.getKey();
                SellerProfileResponse seller = sellerMap.get(sId);

                sellerGroups.add(SellerOrderGroupDto.builder()
                        .sellerId(sId)
                        .sellerStoreName(seller != null ? seller.getStoreName() : "Store #" + sId)
                        .sellerEmail(seller != null ? seller.getEmail() : null)
                        .sellerPhone(seller != null ? seller.getPhone() : null)
                        .items(entry.getValue())
                        .build());
            }

            OrderCreatedNotificationEvent event = OrderCreatedNotificationEvent.builder()
                    .orderId(order.getId())
                    .customerId(order.getCustomerId())
                    .customerName(customerProfile != null ? customerProfile.getName() : null)
                    .customerEmail(customerProfile != null ? customerProfile.getEmail() : null)
                    .customerPhone(order.getPhone())
                    .deliveryAddress(order.getAddress())
                    .subTotal(order.getSubTotal())
                    .discount(order.getDiscount())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(LocalDateTime.now())
                    .sellerGroups(sellerGroups)
                    .build();

            rabbitTemplate.convertAndSend(exchangeName, orderCreatedRoutingKey, event);
            log.info("Published OrderCreated event to RabbitMQ for order #{}", orderId);
        } catch (Exception ex) {
            log.error("Failed to build and publish OrderCreated event for order #{}", orderId, ex);
        }
    }

    public void publishOrderDispatched(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("Cannot publish OrderDispatched event; order #{} not found.", orderId);
                return;
            }

            CustomerProfileResponse customerProfile = null;
            try {
                customerProfile = customerService.getProfile(order.getCustomerId());
            } catch (Exception ex) {
                log.warn("Could not retrieve customer #{} profile for dispatched notification: {}", order.getCustomerId(), ex.getMessage());
            }

            RiderResponse rider = null;
            if (order.getRiderId() != null) {
                try {
                    rider = riderService.getRiderById(order.getRiderId());
                } catch (Exception ex) {
                    log.warn("Could not retrieve rider #{} profile for dispatched notification: {}", order.getRiderId(), ex.getMessage());
                }
            }

            OrderDispatchedNotificationEvent event = OrderDispatchedNotificationEvent.builder()
                    .orderId(order.getId())
                    .customerId(order.getCustomerId())
                    .customerName(customerProfile != null ? customerProfile.getName() : null)
                    .customerEmail(customerProfile != null ? customerProfile.getEmail() : null)
                    .customerPhone(order.getPhone())
                    .deliveryAddress(order.getAddress())
                    .totalAmount(order.getTotalAmount())
                    .riderId(order.getRiderId())
                    .riderName(rider != null ? rider.getName() : null)
                    .riderPhone(rider != null ? rider.getPhone() : null)
                    .vehicleRegistrationNumber(rider != null ? rider.getVehicleRegistrationNumber() : null)
                    .newStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : "ON_THE_WAY")
                    .dispatchedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(exchangeName, orderDispatchedRoutingKey, event);
            log.info("Published OrderDispatched event to RabbitMQ for order #{}", orderId);
        } catch (Exception ex) {
            log.error("Failed to build and publish OrderDispatched event for order #{}", orderId, ex);
        }
    }
}
