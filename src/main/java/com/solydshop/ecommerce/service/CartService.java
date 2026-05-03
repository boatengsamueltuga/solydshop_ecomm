package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.AddToCartRequest;
import com.solydshop.ecommerce.payload.response.CartDTO;

public interface CartService {

    CartDTO addToCart(Long userId, AddToCartRequest request);

    CartDTO getCart(Long userId);

    CartDTO removeFromCart(Long userId, Long productId);

    void clearCart(Long userId);
}
