package com.delicious.domain.review.service;

import com.delicious.domain.review.dto.ReviewRequest;
import com.delicious.domain.review.dto.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);
    List<ReviewResponse> getReviewsByProduct(Long productId);
}
