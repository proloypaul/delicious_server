package com.delicious.domain.coupon.service;

import com.delicious.domain.coupon.dto.CouponValidationResponse;
import com.delicious.domain.coupon.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    CouponValidationResponse validateCoupon(String code, BigDecimal orderAmount);
    Coupon createCoupon(String code, BigDecimal discountAmount, BigDecimal minOrderAmount);
    List<Coupon> getAllCoupons();
}
