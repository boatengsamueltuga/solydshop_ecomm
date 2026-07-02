package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.Order;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.repository.CartRepository;
import com.solydshop.ecommerce.repository.OrderRepository;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private CartRepository    cartRepository;
    @Mock private OrderRepository   orderRepository;
    @Mock private UserRepository    userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl service;

    private Order order;
    private User buyer;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setUserId(7L);
        buyer.setName("Bo Buyer");

        order = new Order();
        ReflectionTestUtils.setField(order, "orderId", 42L);
        order.setUser(buyer);
        order.setTotalAmount(BigDecimal.valueOf(199.99));
        order.setStatus(OrderStatus.PAID);
    }

    @Test
    void updateOrderStatus_notifiesBuyer() {
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateOrderStatus(42L, "SHIPPED");

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        verify(notificationService).createForUser(
                eq(7L), contains("Order #42"), contains("Shipped"), eq("ORDER_STATUS"), eq(42L));
    }

    @Test
    void updateOrderStatus_rejectsInvalidTransition_andDoesNotNotify() {
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> service.updateOrderStatus(42L, "PAYMENT_PENDING"));
        verify(notificationService, never()).createForUser(any(), any(), any(), any(), any());
    }

    @Test
    void updateOrderStatus_blocksCancellingAPaidOrderDirectly() {
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> service.updateOrderStatus(42L, "CANCELLED"));
        verify(notificationService, never()).createForUser(any(), any(), any(), any(), any());
    }

    @Test
    void cancelAfterRefund_notifiesBuyer() {
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cancelAfterRefund(42L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(notificationService).createForUser(
                eq(7L), contains("Order #42"), anyString(), eq("ORDER_STATUS"), eq(42L));
    }
}
