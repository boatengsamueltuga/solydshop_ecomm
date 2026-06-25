package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.*;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.response.CartItemDTO;
import com.solydshop.ecommerce.payload.response.OrderDTO;
import com.solydshop.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(CartRepository cartRepository,
                            OrderRepository orderRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderDTO createPendingOrder(Long userId, String shippingAddress) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(user.getName());
        order.setCustomerEmail(user.getEmail());
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        double total = 0;

        for (CartItem cartItem : cart.getCartItems()) {

            Product product = productRepository
                    .findByIdForUpdate(cartItem.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException(product.getProductName() + " is out of stock");
            }

            // Reserve inventory — released on payment failure, permanent on success
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            order.getOrderItems().add(orderItem);
            total += cartItem.getQuantity() * product.getPrice();
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        return mapToDTO(order);
    }

    @Override
    @Transactional
    public void attachPaymentIntent(Long orderId, String paymentIntentId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStripePaymentIntentId(paymentIntentId);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void confirmPayment(String paymentIntentId) {

        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for PI: " + paymentIntentId));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return; // idempotent — already processed
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Clear the customer's cart now that payment is confirmed
        cartRepository.findByUserId(order.getUser().getUserId()).ifPresent(cart -> {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void failPayment(String paymentIntentId) {

        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for PI: " + paymentIntentId));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return; // idempotent
        }

        // Restore reserved inventory
        for (OrderItem item : order.getOrderItems()) {
            productRepository.findByIdForUpdate(item.getProduct().getProductId()).ifPresent(product ->
                    product.setQuantity(product.getQuantity() + item.getQuantity())
            );
        }

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }

    @Override
    public List<OrderDTO> getUserOrders(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByUser(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

            // Admin manages fulfillment only — payment transitions are automatic via webhook
            Set<OrderStatus> adminAllowed = Set.of(
                    OrderStatus.PROCESSING,
                    OrderStatus.SHIPPED,
                    OrderStatus.DELIVERED,
                    OrderStatus.CANCELLED
            );

            if (!adminAllowed.contains(newStatus)) {
                throw new RuntimeException("Invalid status transition: " + newStatus);
            }

            order.setStatus(newStatus);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }

        orderRepository.save(order);
        return mapToDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToDTO(order);
    }

    private OrderDTO mapToDTO(Order order) {

        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setUserId(order.getUser().getUserId());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setStripePaymentIntentId(order.getStripePaymentIntentId());

        List<CartItemDTO> items = order.getOrderItems()
                .stream()
                .map(item -> {
                    CartItemDTO i = new CartItemDTO();
                    i.setProductId(item.getProduct().getProductId());
                    i.setProductName(item.getProduct().getProductName());
                    i.setQuantity(item.getQuantity());
                    i.setPrice(item.getPrice());
                    i.setImageUrl(item.getProduct().getImageUrl());
                    return i;
                })
                .toList();

        dto.setItems(items);
        return dto;
    }
}
