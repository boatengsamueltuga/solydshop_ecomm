package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductProductIdOrderByCreatedAtDesc(Long productId);

    Optional<Review> findByUserUserIdAndProductProductId(Long userId, Long productId);

    boolean existsByUserUserIdAndProductProductId(Long userId, Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
}
