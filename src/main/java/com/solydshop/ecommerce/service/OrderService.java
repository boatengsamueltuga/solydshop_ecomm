package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.OrderDTO;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    BigDecimal getCartTotal(Long userId);

    OrderDTO createPendingOrderWithPI(Long userId, String shippingAddress, String paymentIntentId);

    void confirmPayment(String paymentIntentId);

    void failPayment(String paymentIntentId);

    void cancelAfterRefund(Long orderId);

    List<OrderDTO> getUserOrders(Long userId);

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getSellerOrders(Long sellerId);

    OrderDTO getOrderById(Long orderId);

    OrderDTO updateOrderStatus(Long orderId, String status);
}
