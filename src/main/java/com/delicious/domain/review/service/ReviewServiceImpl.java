package com.delicious.domain.review.service;

import com.delicious.domain.customer.dto.CustomerProfileResponse;
import com.delicious.domain.customer.service.CustomerService;
import com.delicious.domain.product.service.ProductService;
import com.delicious.domain.review.dto.ReviewRequest;
import com.delicious.domain.review.dto.ReviewResponse;
import com.delicious.domain.review.entity.Review;
import com.delicious.domain.review.mapper.ReviewMapper;
import com.delicious.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final CustomerService customerService;
    private final ProductService productService;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        // Validate product existence (will throw ProductNotFoundException if not exists)
        productService.getProductById(request.getProductId());

        Review review = reviewMapper.toEntity(request);
        Review savedReview = reviewRepository.save(review);

        // Single-review enrichment: one targeted DB call is acceptable on write path
        return enrichSingle(savedReview);
    }

    /**
     * Paginated product reviews with ZERO N+1 queries.
     *
     * <p>Previous implementation called customerService.getProfile(id) inside a stream,
     * causing one DB round-trip per review. For a product with N reviews that was N+1
     * queries total.</p>
     *
     * <p>New approach:
     * <ol>
     *   <li>Fetch one page of reviews (1 query).</li>
     *   <li>Collect all unique customer IDs from that page.</li>
     *   <li>Batch-fetch all customer profiles in ONE query via getProfilesByUserIds().</li>
     *   <li>Enrich each review response from the in-memory map — zero extra DB calls.</li>
     * </ol>
     * Total: 2 queries regardless of page size.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable) {
        // Validate product existence
        productService.getProductById(productId);

        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

        if (reviewPage.isEmpty()) {
            return reviewPage.map(reviewMapper::toResponse);
        }

        // Collect distinct customer IDs from current page only
        Set<Long> customerIds = reviewPage.getContent().stream()
                .map(Review::getCustomerId)
                .collect(Collectors.toSet());

        // Batch fetch: ONE query for all customer profiles on this page
        Map<Long, CustomerProfileResponse> customerMap =
                customerService.getProfilesByUserIds(customerIds);

        // Map reviews to responses using in-memory lookup — no more DB calls
        return reviewPage.map(review -> {
            ReviewResponse response = reviewMapper.toResponse(review);
            CustomerProfileResponse profile = customerMap.get(review.getCustomerId());
            response.setCustomerName(profile != null ? profile.getName() : "Unknown Customer");
            return response;
        });
    }

    /**
     * Single-review enrichment used only on the write path (createReview).
     * Acceptable because it is one review at a time, not a collection.
     */
    private ReviewResponse enrichSingle(Review review) {
        ReviewResponse response = reviewMapper.toResponse(review);
        try {
            CustomerProfileResponse profile = customerService.getProfile(review.getCustomerId());
            response.setCustomerName(profile.getName());
        } catch (Exception e) {
            response.setCustomerName("Unknown Customer");
        }
        return response;
    }
}

