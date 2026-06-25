package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Order;
import com.solydshop.ecommerce.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Page<Order> findAll(Pageable pageable);

    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);
}