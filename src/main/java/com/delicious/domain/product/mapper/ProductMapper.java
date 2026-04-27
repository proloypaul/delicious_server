package com.delicious.domain.product.mapper;

import com.delicious.domain.product.dto.ProductRequest;
import com.delicious.domain.product.dto.ProductResponse;
import com.delicious.domain.product.entity.Category;
import com.delicious.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public Product toEntity(ProductRequest request, Category category) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .foodName(request.getFoodName())
                .description(request.getDescription())
                .price(request.getPrice())
                .makingTime(request.getMakingTime())
                .status(request.getStatus())
                .sellerId(request.getSellerId())
                .category(category)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .foodName(product.getFoodName())
                .description(product.getDescription())
                .price(product.getPrice())
                .makingTime(product.getMakingTime())
                .status(product.getStatus())
                .sellerId(product.getSellerId())
                .category(categoryMapper.toResponse(product.getCategory()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
