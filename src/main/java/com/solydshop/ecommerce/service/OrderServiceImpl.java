package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.*;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.response.CartItemDTO;
import com.solydshop.ecommerce.payload.response.OrderDTO;
import com.solydshop.ecommerce.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(CartRepository cartRepository,
                            OrderRepository orderRepository,
                            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OrderDTO checkout(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);


        order.setStatus(OrderStatus.PENDING);


        double total = 0;

        for (CartItem cartItem : cart.getCartItems()) {

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());

            order.getOrderItems().add(orderItem);

            total += cartItem.getQuantity() * cartItem.getProduct().getPrice();
        }

        order.setTotalAmount(total);

        orderRepository.save(order);

        cart.getCartItems().clear();
        cartRepository.save(cart);

        return mapToDTO(order);
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
    public OrderDTO updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));

        orderRepository.save(order);

        return mapToDTO(order);
    }


    private OrderDTO mapToDTO(Order order) {

        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setTotalAmount(order.getTotalAmount());


        dto.setStatus(order.getStatus().name());


        List<CartItemDTO> items = order.getOrderItems()
                .stream()
                .map(item -> {
                    CartItemDTO i = new CartItemDTO();
                    i.setProductId(item.getProduct().getProductId());
                    i.setProductName(item.getProduct().getProductName());
                    i.setQuantity(item.getQuantity());
                    i.setPrice(item.getPrice());
                    return i;
                })
                .toList();

        dto.setItems(items);

        return dto;
    }
}