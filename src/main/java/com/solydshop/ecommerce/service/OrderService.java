
package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO checkout(Long userId);

    List<OrderDTO> getUserOrders(Long userId);
    OrderDTO updateOrderStatus(Long orderId, String status);
}