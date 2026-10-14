package com.delicious.domain.review.service;

import com.delicious.domain.review.dto.ReviewRequest;
import com.delicious.domain.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);

    /**
     * Returns paginated reviews for a product.
     * Customer names are batch-fetched in one query — no N+1.
     */
    Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable);
}

