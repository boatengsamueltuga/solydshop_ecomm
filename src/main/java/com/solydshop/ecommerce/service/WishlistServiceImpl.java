package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.entity.Wishlist;
import com.solydshop.ecommerce.entity.WishlistItem;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.response.WishlistItemDTO;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import com.solydshop.ecommerce.repository.WishlistItemRepository;
import com.solydshop.ecommerce.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               WishlistItemRepository wishlistItemRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private Wishlist getOrCreateWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return wishlistRepository.findByUserId(user.getUserId())
                .orElseGet(() -> {
                    Wishlist w = new Wishlist();
                    w.setUserId(user.getUserId());
                    return wishlistRepository.save(w);
                });
    }

    private List<WishlistItemDTO> mapToDTO(Wishlist wishlist) {
        return wishlist.getItems().stream().map(item -> {
            WishlistItemDTO dto = new WishlistItemDTO();
            Product p = item.getProduct();
            dto.setProductId(p.getProductId());
            dto.setProductName(p.getProductName());
            dto.setPrice(p.getPrice());
            dto.setImageUrl(p.getImageUrl());
            dto.setQuantity(p.getQuantity());
            dto.setCategoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : null);
            return dto;
        }).toList();
    }

    @Override
    public List<WishlistItemDTO> getWishlist(String email) {
        return mapToDTO(getOrCreateWishlist(email));
    }

    @Override
    public List<WishlistItemDTO> addItem(String email, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        boolean alreadyExists = wishlistItemRepository
                .findByWishlistAndProduct(wishlist, product)
                .isPresent();

        if (!alreadyExists) {
            WishlistItem item = new WishlistItem();
            item.setWishlist(wishlist);
            item.setProduct(product);
            item.setAddedAt(LocalDateTime.now());
            wishlist.getItems().add(item);
            wishlistRepository.save(wishlist);
        }

        return mapToDTO(wishlist);
    }

    @Override
    public List<WishlistItemDTO> removeItem(String email, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        WishlistItem item = wishlistItemRepository
                .findByWishlistAndProduct(wishlist, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in wishlist"));

        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);
        wishlistRepository.save(wishlist);

        return mapToDTO(wishlist);
    }
}
