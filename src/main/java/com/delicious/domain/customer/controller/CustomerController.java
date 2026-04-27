package com.delicious.domain.customer.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.customer.dto.CustomerProfileResponse;
import com.delicious.domain.customer.dto.UpdateCustomerProfileRequest;
import com.delicious.domain.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Endpoints for customer operations")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Get Customer Profile", description = "Retrieves the profile of a customer")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile(@RequestParam Long userId) {
        // Note: userId is temporarily a query param until JWT authentication is fully implemented
        CustomerProfileResponse response = customerService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }

    @Operation(summary = "Update Customer Profile", description = "Updates the name, phone, or address of a customer")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateProfile(
            @RequestParam Long userId,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        
        // Note: userId is temporarily a query param until JWT authentication is fully implemented
        CustomerProfileResponse response = customerService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }
}
