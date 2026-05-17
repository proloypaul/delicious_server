package com.delicious.domain.order.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.order.dto.OrderRequest;
import com.delicious.domain.order.dto.OrderResponse;
import com.delicious.domain.order.dto.OrderStatusUpdateRequest;
import com.delicious.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Endpoints for managing orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestParam Long customerId) {
        OrderResponse response = orderService.createOrder(request, customerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Order retrieved successfully"));
    }

    @GetMapping("/customer")
    @Operation(summary = "Get all orders for the customer")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByCustomer(
            @RequestParam Long customerId,
            Pageable pageable) {
        Page<OrderResponse> response = orderService.getOrdersByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer orders retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all orders (admin/system)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(Pageable pageable) {
        Page<OrderResponse> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "All orders retrieved successfully"));
    }

    @PutMapping("/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse response = orderService.updateOrderStatus(request.getOrderId(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated successfully"));
    }
}
