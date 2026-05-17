package com.delicious.domain.review.mapper;

import com.delicious.domain.review.dto.ReviewRequest;
import com.delicious.domain.review.dto.ReviewResponse;
import com.delicious.domain.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewRequest request) {
        return Review.builder()
                .rating(request.getRating())
                .message(request.getMessage())
                .productId(request.getProductId())
                .customerId(request.getCustomerId())
                .build();
    }

    public ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .message(review.getMessage())
                .productId(review.getProductId())
                .customerId(review.getCustomerId())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
