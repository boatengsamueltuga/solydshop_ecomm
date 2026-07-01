package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.QuoteRequest;
import com.solydshop.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<QuoteRequest, Long> {
    List<QuoteRequest> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<QuoteRequest> findBySellerOrderByCreatedAtDesc(User seller);
    List<QuoteRequest> findAllByOrderByCreatedAtDesc();
    void deleteByProductProductId(Long productId);
}
