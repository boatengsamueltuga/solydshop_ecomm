package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.CheckoutRequest;
import com.solydshop.ecommerce.payload.response.OrderDTO;
import com.solydshop.ecommerce.payload.response.PaymentCheckoutResponse;
import com.solydshop.ecommerce.security.CustomUserDetails;
import com.solydshop.ecommerce.service.OrderService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
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

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentCheckoutResponse> initiateCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) throws StripeException {

        Long userId = userDetails.getUser().getUserId();

        // Create pending order: validates cart, reserves inventory, computes total server-side
        OrderDTO order = orderService.createPendingOrder(userId, request.getShippingAddress());

        long amountCents = Math.round(order.getTotalAmount() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("usd")
                .addPaymentMethodType("card")
                .putMetadata("orderId", String.valueOf(order.getOrderId()))
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        orderService.attachPaymentIntent(order.getOrderId(), intent.getId());

        return ResponseEntity.ok(new PaymentCheckoutResponse(intent.getClientSecret(), order.getOrderId()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            return ResponseEntity.badRequest().body("Webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                if (stripeObject instanceof PaymentIntent pi) {
                    orderService.confirmPayment(pi.getId());
                }
            }
            case "payment_intent.payment_failed" -> {
                if (stripeObject instanceof PaymentIntent pi) {
                    orderService.failPayment(pi.getId());
                }
            }
        }

        return ResponseEntity.ok("ok");
    }
}
