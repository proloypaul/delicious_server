package com.delicious.domain.review.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String message;
    private Long productId;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
