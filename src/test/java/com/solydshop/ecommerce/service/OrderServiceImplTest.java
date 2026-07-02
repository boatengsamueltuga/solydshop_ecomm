package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.Order;
import com.solydshop.ecommerce.entity.OrderItem;
import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.payload.response.OrderDTO;
import com.solydshop.ecommerce.repository.CartRepository;
import com.solydshop.ecommerce.repository.OrderItemRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private CartRepository      cartRepository;
    @Mock private OrderRepository     orderRepository;
    @Mock private UserRepository      userRepository;
    @Mock private ProductRepository   productRepository;
    @Mock private OrderItemRepository orderItemRepository;
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

    @Test
    void getSellerOrders_scopesToOnlyTheSellersOwnItems() {
        Product mine = new Product();
        mine.setProductId(1L);

        OrderItem myItem = new OrderItem();
        myItem.setOrder(order);
        myItem.setProduct(mine);
        myItem.setQuantity(2);
        myItem.setPrice(BigDecimal.valueOf(50));
        myItem.setProductNameSnapshot("My Widget");

        when(orderItemRepository.findSellerOrderItems(9L)).thenReturn(List.of(myItem));

        List<OrderDTO> result = service.getSellerOrders(9L);

        assertEquals(1, result.size());
        OrderDTO dto = result.get(0);
        assertEquals(42L, dto.getOrderId());
        assertEquals(1, dto.getItems().size());
        assertEquals("My Widget", dto.getItems().get(0).getProductName());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(dto.getTotalAmount()));
    }

    @Test
    void getSellerOrders_groupsMultipleItemsFromTheSameOrderAndSumsOnlyThem() {
        Product p1 = new Product(); p1.setProductId(1L);
        Product p2 = new Product(); p2.setProductId(2L);

        OrderItem item1 = new OrderItem();
        item1.setOrder(order); item1.setProduct(p1); item1.setQuantity(1); item1.setPrice(BigDecimal.TEN);
        OrderItem item2 = new OrderItem();
        item2.setOrder(order); item2.setProduct(p2); item2.setQuantity(3); item2.setPrice(BigDecimal.valueOf(5));

        when(orderItemRepository.findSellerOrderItems(9L)).thenReturn(List.of(item1, item2));

        List<OrderDTO> result = service.getSellerOrders(9L);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getItems().size());
        // 10*1 + 5*3 = 25 -- NOT the full order total, just this seller's share
        assertEquals(0, BigDecimal.valueOf(25).compareTo(result.get(0).getTotalAmount()));
    }
}
