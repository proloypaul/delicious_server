package com.delicious.domain.product.mapper;

import com.delicious.domain.product.dto.CategoryResponse;
import com.delicious.domain.product.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .image(category.getImage())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
