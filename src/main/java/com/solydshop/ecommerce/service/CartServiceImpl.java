package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Cart;
import com.solydshop.ecommerce.entity.CartItem;
import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.AddToCartRequest;
import com.solydshop.ecommerce.payload.response.CartDTO;
import com.solydshop.ecommerce.payload.response.CartItemDTO;
import com.solydshop.ecommerce.repository.CartItemRepository;
import com.solydshop.ecommerce.repository.CartRepository;
import com.solydshop.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // -------------------- HELPER METHODS --------------------

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());

        // SAFE handling of cartItems
        List<CartItemDTO> items = cart.getCartItems()
                .stream()
                .map(item -> {
                    CartItemDTO itemDTO = new CartItemDTO();
                    itemDTO.setProductId(item.getProduct().getProductId());
                    itemDTO.setProductName(item.getProduct().getProductName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getProduct().getPrice());
                    return itemDTO;
                })
                .toList();

        dto.setItems(items);

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        dto.setTotalPrice(total);

        return dto;
    }

    // -------------------- CORE METHODS --------------------

    @Override
    public CartDTO addToCart(Long userId, AddToCartRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            cart.getCartItems().add(item);
        }

        cartRepository.save(cart);

        return mapToDTO(cart);
    }

    @Override
    public CartDTO getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found for user: " + userId));

        return mapToDTO(cart);
    }

    @Override
    public CartDTO removeFromCart(Long userId, Long productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found for user: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + productId));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not in cart"));

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);

        // ensure DB consistency
        cartRepository.save(cart);

        return mapToDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found for user: " + userId));

        // rely on orphanRemoval
        cart.getCartItems().clear();

        cartRepository.save(cart);
    }
}