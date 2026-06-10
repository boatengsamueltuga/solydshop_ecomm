package com.solydshop.ecommerce.payload.request;

import lombok.Data;

@Data
public class PaymentIntentRequest {
    private Long amount;   // amount in cents (e.g. $10.00 → 1000)
    private String currency;
}
