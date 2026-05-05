package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Order;
import com.solydshop.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
