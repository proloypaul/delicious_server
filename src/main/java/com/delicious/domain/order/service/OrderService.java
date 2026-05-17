package com.delicious.domain.order.service;

import com.delicious.domain.order.dto.OrderRequest;
import com.delicious.domain.order.dto.OrderResponse;
import com.delicious.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request, Long customerId);
    OrderResponse getOrderById(Long id);
    Page<OrderResponse> getOrdersByCustomer(Long customerId, Pageable pageable);
    Page<OrderResponse> getOrdersByRider(Long riderId, Pageable pageable);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
}
