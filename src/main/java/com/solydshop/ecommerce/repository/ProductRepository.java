package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.ProductStatus;

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

    // Public storefront — always filters to ACTIVE only
    @Query("SELECT p FROM Product p WHERE " +
           "(CAST(:keyword AS string) IS NULL OR " +
           "  LOWER(p.productName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "  LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "  LOWER(p.partNumber)  LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) AND " +
           "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStock = false OR p.quantity > 0) AND " +
           "p.status = com.solydshop.ecommerce.entity.ProductStatus.ACTIVE")
    Page<Product> findAllWithFilters(
            @Param("keyword")    String  keyword,
            @Param("categoryId") Long    categoryId,
            @Param("maxPrice")   Double  maxPrice,
            @Param("inStock")    boolean inStock,
            Pageable pageable
    );

    // Admin view — all products, optional status filter and keyword
    @Query("SELECT p FROM Product p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(CAST(:keyword AS string) IS NULL OR " +
           "  LOWER(p.productName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "  LOWER(p.modelNumber) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "  LOWER(p.partNumber)  LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))")
    Page<Product> findAllForAdmin(
            @Param("status")  ProductStatus status,
            @Param("keyword") String        keyword,
            Pageable pageable
    );
}
