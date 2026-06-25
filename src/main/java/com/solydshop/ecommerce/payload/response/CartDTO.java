package com.solydshop.ecommerce.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class CartDTO {

    private Long cartId;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
