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

    // Only the seller's own line items -- other sellers' items in the same
    // multi-vendor order are never returned. Excludes orders that never
    // completed payment (nothing to fulfill yet).
    @Query("SELECT oi FROM OrderItem oi " +
           "WHERE oi.product.seller.userId = :sellerId " +
           "AND oi.order.status NOT IN (com.solydshop.ecommerce.OrderStatus.OrderStatus.PAYMENT_PENDING, " +
           "                            com.solydshop.ecommerce.OrderStatus.OrderStatus.PAYMENT_FAILED) " +
           "ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findSellerOrderItems(@Param("sellerId") Long sellerId);
}
