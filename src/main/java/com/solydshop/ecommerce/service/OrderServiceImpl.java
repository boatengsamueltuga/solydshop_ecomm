package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.*;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.response.CartItemDTO;
import com.solydshop.ecommerce.payload.response.OrderDTO;
import com.solydshop.ecommerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public OrderServiceImpl(CartRepository cartRepository,
                            OrderRepository orderRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            NotificationService notificationService) {
        this.cartRepository      = cartRepository;
        this.orderRepository     = orderRepository;
        this.userRepository      = userRepository;
        this.productRepository   = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        return cart.getCartItems().stream()
                .map(item -> BigDecimal.valueOf(item.getProduct().getPrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Fix 1 & 2: PI-first flow — order created with PI ID in a single atomic transaction.
    // If this throws, the caller cancels the Stripe PI. No orphaned orders, no zombie PIs.
    @Override
    @Transactional
    public OrderDTO createPendingOrderWithPI(Long userId, String shippingAddress, String paymentIntentId) {

        // Fix 6: pessimistic lock on cart prevents concurrent cart modifications
        Cart cart = cartRepository.findByUserIdForUpdate(userId)
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
        order.setStripePaymentIntentId(paymentIntentId);

        // Fix 3: BigDecimal arithmetic for exact monetary totals
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {

            // Fix 4 (inventory): pessimistic lock prevents overselling under concurrent orders
            Product product = productRepository
                    .findByIdForUpdate(cartItem.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException(product.getProductName() + " is out of stock");
            }

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());

            BigDecimal itemPrice = BigDecimal.valueOf(product.getPrice());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(itemPrice);

            order.getOrderItems().add(orderItem);
            total = total.add(itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        return mapToDTO(order);
    }

    // Fix 4: pessimistic lock on order row prevents duplicate webhook processing
    @Override
    @Transactional
    public void confirmPayment(String paymentIntentId) {

        Order order = orderRepository.findByStripePaymentIntentIdForUpdate(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for PI: " + paymentIntentId));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return; // idempotent — already processed
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        cartRepository.findByUserId(order.getUser().getUserId()).ifPresent(cart -> {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });

        dispatchOrderNotifications(order);
    }

    private String formatAmount(java.math.BigDecimal amount) {
        if (amount.compareTo(java.math.BigDecimal.valueOf(1_000_000)) >= 0) {
            return String.format("$%,.2fM", amount.divide(java.math.BigDecimal.valueOf(1_000_000), 2, java.math.RoundingMode.HALF_UP));
        }
        return String.format("$%,.2f", amount);
    }

    private void dispatchOrderNotifications(Order order) {
        try {
            String title   = "New Order #" + order.getOrderId();
            String message = formatAmount(order.getTotalAmount()) + " from " + order.getCustomerName();

            List<User> admins  = userRepository.findByRoleName("ROLE_ADMIN");
            Set<Long>  adminIds = admins.stream().map(User::getUserId).collect(Collectors.toSet());

            log.info("Dispatching NEW_ORDER notifications for order #{} to {} admin(s)", order.getOrderId(), adminIds.size());

            adminIds.forEach(adminId ->
                    notificationService.createForUser(adminId, title, message, "NEW_ORDER"));

            Set<Long> sellerIds = order.getOrderItems().stream()
                    .map(item -> item.getProduct().getSeller())
                    .filter(seller -> seller != null)
                    .map(User::getUserId)
                    .filter(id -> !adminIds.contains(id))
                    .collect(Collectors.toSet());

            sellerIds.forEach(sellerId ->
                    notificationService.createForUser(sellerId, title, message, "NEW_ORDER"));

        } catch (Exception e) {
            log.error("Failed to dispatch notifications for order #{}: {}", order.getOrderId(), e.getMessage(), e);
        }
    }

    // Fix 4: pessimistic lock on order row prevents concurrent webhook race
    @Override
    @Transactional
    public void failPayment(String paymentIntentId) {

        Order order = orderRepository.findByStripePaymentIntentIdForUpdate(paymentIntentId)
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

    // Fix 5: used only after a Stripe refund has already been issued
    @Override
    @Transactional
    public void cancelAfterRefund(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public List<OrderDTO> getUserOrders(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> getAllOrders() {

        return orderRepository.findAllByOrderByCreatedAtDesc()
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

            // Fix 5: PAID orders must go through the refund endpoint before cancellation
            if (newStatus == OrderStatus.CANCELLED && order.getStatus() == OrderStatus.PAID) {
                throw new RuntimeException(
                        "Paid orders must be refunded before cancellation. Use the refund endpoint.");
            }

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
