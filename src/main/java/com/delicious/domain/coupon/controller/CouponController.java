package com.delicious.domain.coupon.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.coupon.dto.CouponValidationResponse;
import com.delicious.domain.coupon.entity.Coupon;
import com.delicious.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon API", description = "Endpoints for promo coupon validation and management")
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "Validate a promo coupon")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "0") BigDecimal orderAmount) {
        CouponValidationResponse response = couponService.validateCoupon(code, orderAmount);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * GET /api/v1/coupons?page=0&size=20
     *
     * <p>Returns a paginated list of coupons (admin only).
     * Prevents a full table scan loading all coupons into memory.</p>
     */
    @Operation(
        summary = "Get all coupons (Admin only)",
        description = "Paginated. Supports ?page=&size= query params. Default: page=0, size=20."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Coupon>>> getAllCoupons(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Coupon> coupons = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.success(coupons, "Coupons retrieved successfully"));
    }

    @Operation(summary = "Create a new coupon (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal discountAmount,
            @RequestParam BigDecimal minOrderAmount) {
        Coupon coupon = couponService.createCoupon(code, discountAmount, minOrderAmount);
        return ResponseEntity.ok(ApiResponse.success(coupon, "Coupon created successfully"));
    }
}
