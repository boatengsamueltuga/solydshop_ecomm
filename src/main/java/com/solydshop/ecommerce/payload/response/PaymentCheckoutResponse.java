package com.solydshop.ecommerce.payload.response;

public class PaymentCheckoutResponse {

    private String clientSecret;
    private Long orderId;

    public PaymentCheckoutResponse(String clientSecret, Long orderId) {
        this.clientSecret = clientSecret;
        this.orderId = orderId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Long getOrderId() {
        return orderId;
    }
}
