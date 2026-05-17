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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        return enrichReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        // Validate product existence
        productService.getProductById(productId);

        return reviewRepository.findByProductId(productId).stream()
                .map(this::enrichReviewResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse enrichReviewResponse(Review review) {
        ReviewResponse response = reviewMapper.toResponse(review);
        try {
            CustomerProfileResponse customerProfile = customerService.getProfile(review.getCustomerId());
            response.setCustomerName(customerProfile.getName());
        } catch (Exception e) {
            response.setCustomerName("Unknown Customer");
        }
        return response;
    }
}
