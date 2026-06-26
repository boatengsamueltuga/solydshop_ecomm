package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.CheckoutRequest;
import com.solydshop.ecommerce.payload.request.PaymentIntentRequest;
import com.solydshop.ecommerce.payload.response.OrderDTO;
import com.solydshop.ecommerce.payload.response.PaymentCheckoutResponse;
import com.solydshop.ecommerce.payload.response.PaymentIntentResponse;
import com.solydshop.ecommerce.security.CustomUserDetails;
import com.solydshop.ecommerce.service.OrderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentCheckoutResponse> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) throws StripeException {

        Long userId = userDetails.getUser().getUserId();

        OrderDTO order = orderService.checkout(userId, request.getShippingAddress());

        long amountCents = Math.round(order.getTotalAmount() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("usd")
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        return ResponseEntity.ok(new PaymentCheckoutResponse(intent.getClientSecret(), order.getOrderId()));
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestBody PaymentIntentRequest request) throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency() != null ? request.getCurrency() : "usd")
                .addPaymentMethodType("card")
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return ResponseEntity.ok(new PaymentIntentResponse(paymentIntent.getClientSecret()));
    }
}
