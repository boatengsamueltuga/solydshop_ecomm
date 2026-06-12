package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findBySellerUserId(Long userId, Pageable pageable);

    Page<Product> findByCategoryCategoryId(Long categoryId, Pageable pageable);

    // Search by keyword across name, model number, and part number
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.partNumber)  LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Search by keyword across name, model number, and part number, filtered by category
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND (" +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.partNumber)  LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}