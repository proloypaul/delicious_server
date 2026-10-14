package com.delicious.domain.review.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.review.dto.ReviewRequest;
import com.delicious.domain.review.dto.ReviewResponse;
import com.delicious.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "Endpoints for managing product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a new review")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Review submitted successfully"));
    }

    /**
     * GET /api/v1/reviews/product/{id}?page=0&size=10&sort=createdAt,desc
     *
     * <p>Returns paginated reviews for a product. Customer names are batch-enriched —
     * no N+1 queries. Default: page 0, 10 reviews per page, newest first.</p>
     */
    @GetMapping("/product/{id}")
    @Operation(
        summary = "Get paginated reviews for a product",
        description = "Supports ?page=&size=&sort= query params. Default: page=0, size=10, sort by createdAt desc."
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByProduct(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getReviewsByProduct(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Reviews retrieved successfully"));
    }
}

