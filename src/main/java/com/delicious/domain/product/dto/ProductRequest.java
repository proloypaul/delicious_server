package com.delicious.domain.product.dto;

import com.delicious.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Food name is required")
    private String foodName;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Making time is required")
    @Min(value = 1, message = "Making time must be at least 1 minute")
    private Integer makingTime;

    @NotNull(message = "Status is required")
    private ProductStatus status;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
