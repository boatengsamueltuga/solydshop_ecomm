package com.solydshop.ecommerce.entity;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private double totalAmount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {}

    public Long getOrderId() { return orderId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // ===== ADDED GETTER/SETTER =====
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    // =============================

    public List<OrderItem> getOrderItems() { return orderItems; }
}