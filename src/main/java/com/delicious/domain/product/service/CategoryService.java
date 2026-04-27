package com.delicious.domain.product.service;

import com.delicious.domain.product.dto.CategoryRequest;
import com.delicious.domain.product.dto.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    Page<CategoryResponse> getAllCategories(Pageable pageable);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
