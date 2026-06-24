package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.response.WishlistItemDTO;
import com.solydshop.ecommerce.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> getWishlist(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.getWishlist(authentication.getName()));
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<List<WishlistItemDTO>> addItem(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(wishlistService.addItem(authentication.getName(), productId));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<List<WishlistItemDTO>> removeItem(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(wishlistService.removeItem(authentication.getName(), productId));
    }
}
