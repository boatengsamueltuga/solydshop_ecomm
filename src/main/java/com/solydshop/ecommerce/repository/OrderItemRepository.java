package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COUNT(DISTINCT oi.order.orderId) FROM OrderItem oi " +
           "WHERE oi.product.seller.userId = :sellerId AND oi.order.status IN :statuses")
    long countDistinctOpenOrdersBySeller(@Param("sellerId") Long sellerId,
                                          @Param("statuses") List<OrderStatus> statuses);
}
