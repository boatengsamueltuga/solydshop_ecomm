package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.SellerDowngradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerDowngradeRequestRepository extends JpaRepository<SellerDowngradeRequest, Long> {

    Optional<SellerDowngradeRequest> findTopByUserEmailOrderByCreatedAtDesc(String email);

    List<SellerDowngradeRequest> findAllByOrderByCreatedAtDesc();
}
