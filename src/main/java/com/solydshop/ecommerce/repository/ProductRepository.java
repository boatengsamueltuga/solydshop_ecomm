package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Product;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

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

    // Price-filtered variants

    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice AND (" +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.partNumber)  LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeywordWithMaxPrice(
            @Param("keyword") String keyword,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.price <= :maxPrice AND (" +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.partNumber)  LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeywordAndCategoryWithMaxPrice(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    Page<Product> findByCategoryCategoryIdAndPriceLessThanEqual(Long categoryId, Double maxPrice, Pageable pageable);

    Page<Product> findByPriceLessThanEqual(Double maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(CAST(:keyword AS string) IS NULL OR " +
           "  LOWER(p.productName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "  LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "  LOWER(p.partNumber)  LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) AND " +
           "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStock = false OR p.quantity > 0)")
    Page<Product> findAllWithFilters(
            @Param("keyword")    String  keyword,
            @Param("categoryId") Long    categoryId,
            @Param("maxPrice")   Double  maxPrice,
            @Param("inStock")    boolean inStock,
            Pageable pageable
    );
}