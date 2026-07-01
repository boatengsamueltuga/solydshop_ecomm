package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.SellerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {

    Optional<SellerApplication> findTopByUserEmailOrderByCreatedAtDesc(String email);

    List<SellerApplication> findAllByOrderByCreatedAtDesc();
}
