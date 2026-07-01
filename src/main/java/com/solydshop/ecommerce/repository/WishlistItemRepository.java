package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.Wishlist;
import com.solydshop.ecommerce.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByWishlistAndProduct(Wishlist wishlist, Product product);
    void deleteByProduct(Product product);
}
