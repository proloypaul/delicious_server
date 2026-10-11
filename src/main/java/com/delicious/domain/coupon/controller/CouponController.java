package com.delicious.domain.coupon.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.coupon.dto.CouponValidationResponse;
import com.delicious.domain.coupon.entity.Coupon;
import com.delicious.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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

    @Operation(summary = "Get all coupons (Admin only)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {
        List<Coupon> coupons = couponService.getAllCoupons();
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
