package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO checkout(Long userId, String shippingAddress);

    List<OrderDTO> getUserOrders(Long userId);

    List<OrderDTO> getAllOrders();

    OrderDTO getOrderById(Long orderId);

    OrderDTO updateOrderStatus(Long orderId, String status);
}