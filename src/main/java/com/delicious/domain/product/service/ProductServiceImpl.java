package com.delicious.domain.product.service;

import com.delicious.domain.product.dto.ProductRequest;
import com.delicious.domain.product.dto.ProductResponse;
import com.delicious.domain.product.entity.Category;
import com.delicious.domain.product.entity.Product;
import com.delicious.domain.product.mapper.ProductMapper;
import com.delicious.domain.product.repository.CategoryRepository;
import com.delicious.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request, category);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String categoryName, Pageable pageable) {
        if (StringUtils.hasText(categoryName)) {
            // Filtered by category name — single JOIN query via @EntityGraph
            return productRepository.findByCategoryNameIgnoreCase(categoryName, pageable)
                    .map(productMapper::toResponse);
        }
        // Full list — single JOIN query via @EntityGraph
        return productRepository.findAll(pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        product.setFoodName(request.getFoodName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setMakingTime(request.getMakingTime());
        product.setStatus(request.getStatus());
        product.setSellerId(request.getSellerId());
        product.setCategory(category);

        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
