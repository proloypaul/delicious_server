package com.delicious.domain.order.service;

import com.delicious.domain.customer.dto.CustomerProfileResponse;
import com.delicious.domain.customer.service.CustomerService;
import com.delicious.domain.order.dto.CustomerSummaryDto;
import com.delicious.domain.order.dto.OrderRequest;
import com.delicious.domain.order.dto.OrderResponse;
import com.delicious.domain.order.dto.RiderSummaryDto;
import com.delicious.domain.order.entity.Order;
import com.delicious.domain.order.entity.OrderItem;
import com.delicious.domain.order.enums.OrderStatus;
import com.delicious.domain.order.mapper.OrderMapper;
import com.delicious.domain.order.repository.OrderRepository;
import com.delicious.domain.product.entity.Product;
import com.delicious.domain.product.enums.ProductStatus;
import com.delicious.domain.product.repository.ProductRepository;
import com.delicious.domain.rider.dto.RiderResponse;
import com.delicious.domain.rider.service.RiderService;
import com.delicious.domain.order.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final RiderService riderService;
    private final com.delicious.domain.coupon.service.CouponService couponService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long customerId) {
        Order order = Order.builder()
                .phone(request.getPhone())
                .address(request.getAddress())
                .customerId(customerId)
                .orderStatus(OrderStatus.PENDING)
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .build();

        // ACID fix: Batch fetch all requested products within the current transaction boundary
        // and strictly validate that they exist and are in APPROVED status.
        List<Long> productIds = request.getItems().stream()
                .map(item -> item.getProductId())
                .distinct()
                .toList();

        List<Product> products = productRepository.findByIdInAndStatus(productIds, ProductStatus.APPROVED);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal subTotal = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            Product product = productMap.get(itemReq.getProductId());
            if (product == null) {
                throw new IllegalArgumentException(
                        "Product with id " + itemReq.getProductId() + " is not available or not yet approved.");
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subTotal = subTotal.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice()) // Atomic snapshot price from DB entity
                    .build();
            order.addItem(orderItem);
        }

        order.setSubTotal(subTotal);

        // Server-side coupon verification: if couponCode is provided, recalculate valid discount
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            var couponResult = couponService.validateCoupon(request.getCouponCode(), subTotal);
            if (couponResult.isValid()) {
                discount = couponResult.getDiscountAmount();
            } else {
                throw new IllegalArgumentException("Invalid coupon applied: " + couponResult.getMessage());
            }
        } else if (request.getDiscount() != null) {
            discount = request.getDiscount();
        }

        // Ensure discount is not greater than subtotal
        if (discount.compareTo(subTotal) > 0) {
            discount = subTotal;
        }
        order.setDiscount(discount);
        order.setTotalAmount(subTotal.subtract(discount));

        Order savedOrder = orderRepository.save(order);
        return enrichOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return enrichOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomer(Long customerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        return enrichOrdersBatch(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByRider(Long riderId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByRiderId(riderId, pageable);
        return enrichOrdersBatch(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return enrichOrdersBatch(orders);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        order.setOrderStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return enrichOrderResponse(updatedOrder);
    }

    /**
     * Batch enrichment to eliminate N+1 queries when fetching orders for lists/grids.
     */
    private Page<OrderResponse> enrichOrdersBatch(Page<Order> ordersPage) {
        List<Order> orders = ordersPage.getContent();
        if (orders.isEmpty()) {
            return ordersPage.map(orderMapper::toOrderResponse);
        }

        Set<Long> customerIds = orders.stream()
                .map(Order::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> riderIds = orders.stream()
                .map(Order::getRiderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, CustomerProfileResponse> customerMap = customerService.getProfilesByUserIds(customerIds);
        Map<Long, RiderResponse> riderMap = riderService.getRidersByUserIds(riderIds);

        List<OrderResponse> responses = orders.stream().map(order -> {
            OrderResponse resp = orderMapper.toOrderResponse(order);

            CustomerProfileResponse customerProfile = customerMap.get(order.getCustomerId());
            if (customerProfile != null) {
                resp.setCustomer(CustomerSummaryDto.builder()
                        .id(order.getCustomerId())
                        .name(customerProfile.getName())
                        .email(customerProfile.getEmail())
                        .phone(customerProfile.getPhone())
                        .build());
            }

            if (order.getRiderId() != null) {
                RiderResponse rider = riderMap.get(order.getRiderId());
                if (rider != null) {
                    resp.setRider(RiderSummaryDto.builder()
                            .id(rider.getUserId())
                            .name(rider.getName())
                            .phone(rider.getPhone())
                            .vehicleRegistrationNumber(rider.getVehicleRegistrationNumber())
                            .build());
                }
            }

            return resp;
        }).collect(Collectors.toList());

        return new PageImpl<>(responses, ordersPage.getPageable(), ordersPage.getTotalElements());
    }

    private OrderResponse enrichOrderResponse(Order order) {
        OrderResponse response = orderMapper.toOrderResponse(order);

        // Fetch customer details
        try {
            CustomerProfileResponse customerProfile = customerService.getProfile(order.getCustomerId());
            if (customerProfile != null) {
                response.setCustomer(CustomerSummaryDto.builder()
                        .id(order.getCustomerId())
                        .name(customerProfile.getName())
                        .email(customerProfile.getEmail())
                        .phone(customerProfile.getPhone())
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to fetch customer profile for order enrichment, customerId: {}", order.getCustomerId(), e);
        }

        // Fetch rider details
        if (order.getRiderId() != null) {
            try {
                RiderResponse rider = riderService.getRiderById(order.getRiderId());
                if (rider != null) {
                    response.setRider(RiderSummaryDto.builder()
                            .id(rider.getUserId())
                            .name(rider.getName())
                            .phone(rider.getPhone())
                            .vehicleRegistrationNumber(rider.getVehicleRegistrationNumber())
                            .build());
                }
            } catch (Exception e) {
                log.error("Failed to fetch rider profile for order enrichment, riderId: {}", order.getRiderId(), e);
            }
        }

        return response;
    }
}
