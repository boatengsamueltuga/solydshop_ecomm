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
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Fix 6 (thread-safety): set once at startup on the static field, not per-request
    // Fix 8: warn at startup if webhook secret is missing
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("stripe.webhook-secret is not configured — webhook events will be rejected. " +
                     "Run: stripe listen --forward-to localhost:8080/api/payment/webhook");
        }
    }

    // Fix 1 & 2: PI-first flow eliminates the two-transaction window.
    // Crash after PI creation but before order creation → PI expires in 24h, no money taken.
    // Order creation failure → PI cancelled in catch block, no orphaned order.
    @PostMapping("/checkout")
    public ResponseEntity<PaymentCheckoutResponse> initiateCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) throws StripeException {

        Long userId = userDetails.getUser().getUserId();

        // Step 1: read cart total (non-locking, for PI sizing)
        BigDecimal estimatedTotal = orderService.getCartTotal(userId);

        // Fix 3: exact cent conversion using BigDecimal, no floating-point rounding error
        long amountCents = estimatedTotal
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        // Step 2: create Stripe PI (no DB transaction held during network call)
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("usd")
                .addPaymentMethodType("card")
                .putMetadata("userId", String.valueOf(userId))
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        // Step 3: create pending order with PI ID in a single atomic transaction
        // Fix 2: if this throws, cancel the PI so it doesn't linger on Stripe
        OrderDTO order;
        try {
            order = orderService.createPendingOrderWithPI(userId, request.getShippingAddress(), intent.getId());
        } catch (Exception e) {
            try {
                intent.cancel();
            } catch (StripeException se) {
                log.error("Failed to cancel orphaned Stripe PI {} after order creation failure: {}", intent.getId(), se.getMessage());
            }
            throw e;
        }

        // Step 4: verify PI amount matches the locked cart total (catches cart-changed-during-checkout race)
        BigDecimal piTotal = BigDecimal.valueOf(intent.getAmount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (order.getTotalAmount().compareTo(piTotal) != 0) {
            try {
                intent.cancel();
                orderService.failPayment(intent.getId());
            } catch (Exception ex) {
                log.error("Cleanup failed after cart-total mismatch for PI {}: {}", intent.getId(), ex.getMessage());
            }
            throw new RuntimeException("Cart was modified during checkout. Please try again.");
        }

        return ResponseEntity.ok(new PaymentCheckoutResponse(intent.getClientSecret(), order.getOrderId()));
    }

    // Fix 5: refund a PAID order then set it to CANCELLED — admin-only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/refund-and-cancel/{orderId}")
    public ResponseEntity<OrderDTO> refundAndCancel(@PathVariable Long orderId) throws StripeException {

        OrderDTO order = orderService.getOrderById(orderId);

        if (order.getStripePaymentIntentId() == null || order.getStripePaymentIntentId().isBlank()) {
            throw new RuntimeException("No payment found for this order");
        }

        RefundCreateParams refundParams = RefundCreateParams.builder()
                .setPaymentIntent(order.getStripePaymentIntentId())
                .build();

        Refund.create(refundParams);

        orderService.cancelAfterRefund(orderId);

        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // Fix 4: webhook — Stripe can deliver events multiple times; idempotency is enforced
    // via pessimistic lock + status check in the service methods.
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("Webhook received but stripe.webhook-secret is not configured");
            return ResponseEntity.badRequest().body("Webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        try {
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
        } catch (Exception e) {
            // Return 500 so Stripe retries; log for monitoring (Fix 8)
            log.error("Webhook handler failed for event {}: {}", event.getId(), e.getMessage());
            return ResponseEntity.internalServerError().body("Handler failed");
        }

        return ResponseEntity.ok("ok");
    }
}
