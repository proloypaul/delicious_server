package com.delicious.domain.coupon.service;

import com.delicious.domain.coupon.dto.CouponValidationResponse;
import com.delicious.domain.coupon.entity.Coupon;
import com.delicious.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code, BigDecimal orderAmount) {
        if (code == null || code.trim().isEmpty()) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("Coupon code cannot be empty")
                    .build();
        }

        Optional<Coupon> opt = couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim());
        if (opt.isEmpty()) {
            return CouponValidationResponse.builder()
                    .code(code)
                    .valid(false)
                    .message("Invalid or inactive coupon code")
                    .build();
        }

        Coupon coupon = opt.get();
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            return CouponValidationResponse.builder()
                    .code(coupon.getCode())
                    .valid(false)
                    .message("This coupon has expired")
                    .build();
        }

        if (orderAmount != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return CouponValidationResponse.builder()
                    .code(coupon.getCode())
                    .discountAmount(coupon.getDiscountAmount())
                    .minOrderAmount(coupon.getMinOrderAmount())
                    .valid(false)
                    .message("Min order amount for " + coupon.getCode() + " is " + coupon.getMinOrderAmount() + " BDT.")
                    .build();
        }

        return CouponValidationResponse.builder()
                .code(coupon.getCode())
                .discountAmount(coupon.getDiscountAmount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .valid(true)
                .message("Promo coupon " + coupon.getCode() + " applied! Saved " + coupon.getDiscountAmount() + " BDT.")
                .build();
    }

    @Override
    @Transactional
    public Coupon createCoupon(String code, BigDecimal discountAmount, BigDecimal minOrderAmount) {
        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Coupon code already exists: " + code);
        }
        Coupon coupon = Coupon.builder()
                .code(code.toUpperCase().trim())
                .discountAmount(discountAmount)
                .minOrderAmount(minOrderAmount)
                .active(true)
                .build();
        return couponRepository.save(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Coupon> getAllCoupons(Pageable pageable) {
        // Paginated fetch — prevents loading every coupon into JVM memory at once.
        // Without Pageable, couponRepository.findAll() would do a full table scan.
        return couponRepository.findAll(pageable);
    }
}
