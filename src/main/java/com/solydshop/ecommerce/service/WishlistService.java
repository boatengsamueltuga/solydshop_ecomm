package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.WishlistItemDTO;

import java.util.List;

public interface WishlistService {
    List<WishlistItemDTO> getWishlist(String email);
    List<WishlistItemDTO> addItem(String email, Long productId);
    List<WishlistItemDTO> removeItem(String email, Long productId);
}
