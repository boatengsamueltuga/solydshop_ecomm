package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Cart;
import com.solydshop.ecommerce.entity.CartItem;
import com.solydshop.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
