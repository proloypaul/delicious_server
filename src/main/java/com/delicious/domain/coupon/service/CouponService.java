package com.delicious.domain.coupon.service;

import com.delicious.domain.coupon.dto.CouponValidationResponse;
import com.delicious.domain.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CouponService {
    CouponValidationResponse validateCoupon(String code, BigDecimal orderAmount);
    Coupon createCoupon(String code, BigDecimal discountAmount, BigDecimal minOrderAmount);

    /**
     * Returns a paginated list of coupons.
     * Prevents loading all coupons into memory (full table scan).
     */
    Page<Coupon> getAllCoupons(Pageable pageable);
}

