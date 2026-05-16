package com.delicious.domain.seller.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.product.dto.ProductResponse;
import com.delicious.domain.seller.dto.SellerProfileResponse;
import com.delicious.domain.seller.dto.SellerRegistrationRequest;
import com.delicious.domain.seller.dto.UpdateSellerProfileRequest;
import com.delicious.domain.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@Tag(name = "Sellers", description = "Endpoints for seller management and operations")
public class SellerController {

    private final SellerService sellerService;
    private final com.delicious.domain.product.service.ProductService productService;

    @Operation(summary = "Register a new seller", description = "Default status will be INACTIVE")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> register(@Valid @RequestBody SellerRegistrationRequest request) {
        SellerProfileResponse response = sellerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Seller registered successfully. Account is pending approval."));
    }

    @Operation(summary = "Get seller profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getProfile(@RequestParam Long userId) {
        SellerProfileResponse response = sellerService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }

    @Operation(summary = "Update seller profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> updateProfile(
            @RequestParam Long userId,
            @Valid @RequestBody UpdateSellerProfileRequest request) {
        SellerProfileResponse response = sellerService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @Operation(summary = "Get products belonging to the seller")
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getSellerProducts(
            @RequestParam Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductResponse> products = productService.getProductsBySellerId(userId, pageable);
        String message = products.isEmpty() ? "seller don't have any product" : "Seller products retrieved successfully";
        return ResponseEntity.ok(ApiResponse.success(products, message));
    }
}
