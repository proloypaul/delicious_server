package com.delicious.domain.product.service;

import com.delicious.domain.product.dto.ProductRequest;
import com.delicious.domain.product.dto.ProductResponse;
import com.delicious.domain.product.entity.Category;
import com.delicious.domain.product.entity.Product;
import com.delicious.domain.product.mapper.ProductMapper;
import com.delicious.domain.product.repository.CategoryRepository;
import com.delicious.domain.product.repository.ProductRepository;
import com.delicious.domain.seller.dto.SellerProfileResponse;
import com.delicious.domain.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final @Lazy SellerService sellerService; // Use @Lazy to prevent potential circular dependency if SellerService ever needs ProductService back

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request, category);
        Product saved = productRepository.save(product);
        return enrichWithSellerInfo(productMapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String categoryName, Pageable pageable) {
        Page<Product> products;
        if (StringUtils.hasText(categoryName)) {
            products = productRepository.findByCategoryNameIgnoreCase(categoryName, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        
        return enrichPageWithSellerInfo(products.map(productMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return enrichWithSellerInfo(productMapper.toResponse(product));
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
        return enrichWithSellerInfo(productMapper.toResponse(updated));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsBySellerId(Long sellerId, Pageable pageable) {
        Page<ProductResponse> responses = productRepository.findBySellerId(sellerId, pageable)
                .map(productMapper::toResponse);
        return enrichPageWithSellerInfo(responses);
    }

    private ProductResponse enrichWithSellerInfo(ProductResponse response) {
        if (response == null || response.getSellerId() == null) return response;
        
        try {
            SellerProfileResponse seller = sellerService.getProfile(response.getSellerId());
            response.setSellerName(seller.getName());
            response.setStoreName(seller.getStoreName());
        } catch (Exception e) {
            // Log and continue if seller info cannot be fetched
            System.err.println("Failed to fetch seller info for sellerId: " + response.getSellerId());
        }
        return response;
    }

    private Page<ProductResponse> enrichPageWithSellerInfo(Page<ProductResponse> page) {
        List<Long> sellerIds = page.getContent().stream()
                .map(ProductResponse::getSellerId)
                .distinct()
                .collect(Collectors.toList());

        if (sellerIds.isEmpty()) return page;

        try {
            Map<Long, SellerProfileResponse> sellerMap = sellerService.getSellerProfilesByUserIds(sellerIds);
            page.getContent().forEach(p -> {
                SellerProfileResponse seller = sellerMap.get(p.getSellerId());
                if (seller != null) {
                    p.setSellerName(seller.getName());
                    p.setStoreName(seller.getStoreName());
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to fetch seller info in batch for sellerIds: " + sellerIds);
        }

        return page;
    }
}
