package com.delicious.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not be greater than 5")
    private Integer rating;

    @NotBlank(message = "Review message must not be blank")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;
}
