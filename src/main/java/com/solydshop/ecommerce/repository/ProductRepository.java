package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findBySellerUserId(Long userId, Pageable pageable);
}