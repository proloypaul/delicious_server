package com.delicious.domain.rider.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.order.dto.OrderResponse;
import com.delicious.domain.rider.dto.DeliveryStatusUpdateRequest;
import com.delicious.domain.rider.dto.RiderRegistrationRequest;
import com.delicious.domain.rider.dto.RiderResponse;
import com.delicious.domain.rider.service.RiderService;
import com.delicious.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/riders")
@RequiredArgsConstructor
@Tag(name = "Rider API", description = "Endpoints for managing riders and deliveries")
public class RiderController {

    private final RiderService riderService;

    @PostMapping
    @Operation(summary = "Register a new rider")
    public ResponseEntity<ApiResponse<RiderResponse>> registerRider(
            @Valid @RequestBody RiderRegistrationRequest request) {
        RiderResponse response = riderService.registerRider(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Rider registered successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all riders")
    public ResponseEntity<ApiResponse<Page<RiderResponse>>> getAllRiders(Pageable pageable) {
        Page<RiderResponse> response = riderService.getAllRiders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Riders retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RIDER')")
    @Operation(summary = "Get rider by ID")
    public ResponseEntity<ApiResponse<RiderResponse>> getRiderById(@PathVariable Long id) {
        RiderResponse response = riderService.getRiderById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Rider profile retrieved successfully"));
    }

    @GetMapping("/orders/{riderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RIDER')")
    @Operation(summary = "Get orders assigned to a rider")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getRiderOrders(
            @PathVariable Long riderId,
            Pageable pageable) {
        Page<OrderResponse> response = riderService.getRiderOrders(riderId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Rider orders retrieved successfully"));
    }

    @PutMapping("/status/{orderId}")
    @PreAuthorize("hasRole('RIDER')")
    @Operation(summary = "Update delivery status for an order")
    public ResponseEntity<ApiResponse<OrderResponse>> updateDeliveryStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request,
            @AuthenticationPrincipal User user) {
        OrderResponse response = riderService.updateDeliveryStatus(orderId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Delivery status updated successfully"));
    }
}
