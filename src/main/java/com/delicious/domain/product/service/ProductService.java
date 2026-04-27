package com.delicious.domain.product.service;

import com.delicious.domain.product.dto.ProductRequest;
import com.delicious.domain.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    Page<ProductResponse> getAllProducts(String categoryName, Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
