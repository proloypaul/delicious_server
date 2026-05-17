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
import com.delicious.domain.product.dto.ProductResponse;
import com.delicious.domain.product.service.ProductService;
import com.delicious.domain.rider.dto.RiderResponse;
import com.delicious.domain.rider.service.RiderService;
import com.delicious.domain.order.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;
    private final ProductService productService;
    private final RiderService riderService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            CustomerService customerService,
                            ProductService productService,
                            @org.springframework.context.annotation.Lazy RiderService riderService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.customerService = customerService;
        this.productService = productService;
        this.riderService = riderService;
    }

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

        BigDecimal subTotal = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            ProductResponse product = productService.getProductById(itemReq.getProductId());
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subTotal = subTotal.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice())
                    .build();
            order.addItem(orderItem);
        }

        order.setSubTotal(subTotal);
        
        // Ensure discount is not greater than subtotal
        BigDecimal discount = order.getDiscount();
        if (discount.compareTo(subTotal) > 0) {
            discount = subTotal;
            order.setDiscount(discount);
        }
        
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
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::enrichOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByRider(Long riderId, Pageable pageable) {
        return orderRepository.findByRiderId(riderId, pageable)
                .map(this::enrichOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::enrichOrderResponse);
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

    private OrderResponse enrichOrderResponse(Order order) {
        OrderResponse response = orderMapper.toOrderResponse(order);
        
        // Fetch and set customer details
        try {
            CustomerProfileResponse customerProfile = customerService.getProfile(order.getCustomerId());
            response.setCustomer(CustomerSummaryDto.builder()
                    .id(order.getCustomerId())
                    .name(customerProfile.getName())
                    .email(customerProfile.getEmail())
                    .phone(customerProfile.getPhone())
                    .build());
        } catch (Exception e) {
            // Handle gracefully if customer not found
        }

        // Fetch and set rider details
        if (order.getRiderId() != null) {
            try {
                RiderResponse rider = riderService.getRiderById(order.getRiderId());
                response.setRider(RiderSummaryDto.builder()
                        .id(rider.getUserId())
                        .name(rider.getName())
                        .phone(rider.getPhone())
                        .vehicleRegistrationNumber(rider.getVehicleRegistrationNumber())
                        .build());
            } catch (Exception e) {
                // Handle gracefully if rider not found
            }
        }

        return response;
    }
}
