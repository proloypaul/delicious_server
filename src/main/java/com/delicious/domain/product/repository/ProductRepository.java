package com.delicious.domain.product.repository;

import com.delicious.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Fetch all products with category JOIN in a single query — prevents N+1
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAll(Pageable pageable);

    // Filter by category name (case-insensitive) — single JOIN query, no N+1
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByCategoryNameIgnoreCase(String categoryName, Pageable pageable);
}
