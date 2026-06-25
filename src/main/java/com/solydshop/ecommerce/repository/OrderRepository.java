package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Order;
import com.solydshop.ecommerce.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Page<Order> findAll(Pageable pageable);

    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.stripePaymentIntentId = :piId")
    Optional<Order> findByStripePaymentIntentIdForUpdate(@Param("piId") String piId);
}