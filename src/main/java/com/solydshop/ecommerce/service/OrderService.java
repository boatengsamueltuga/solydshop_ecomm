package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO createPendingOrder(Long userId, String shippingAddress);

    void attachPaymentIntent(Long orderId, String paymentIntentId);

    void confirmPayment(String paymentIntentId);

    void failPayment(String paymentIntentId);

    List<OrderDTO> getUserOrders(Long userId);

    List<OrderDTO> getAllOrders();

    OrderDTO getOrderById(Long orderId);

    OrderDTO updateOrderStatus(Long orderId, String status);
}
