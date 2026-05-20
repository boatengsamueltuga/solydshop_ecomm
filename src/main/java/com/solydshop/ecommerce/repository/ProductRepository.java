package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Fetch products created by seller
    Page<Product> findBySellerUserId(
            Long userId,
            Pageable pageable
    );

    // Search products by product name
    Page<Product> findByProductNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    // Filter products by category
    Page<Product> findByCategoryCategoryId(
            Long categoryId,
            Pageable pageable
    );

    // Search products by keyword and category
    Page<Product> findByProductNameContainingIgnoreCaseAndCategoryCategoryId(
            String keyword,
            Long categoryId,
            Pageable pageable
    );
}