package com.solydshop.ecommerce.payload.response;

import java.util.List;

public class OrderDTO {

    private Long orderId;
    private double totalAmount;
    private List<CartItemDTO> items;


    private String status;


    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }


    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}