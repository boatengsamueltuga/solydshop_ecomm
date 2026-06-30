package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.QuoteRequestPayload;
import com.solydshop.ecommerce.payload.request.QuoteRespondPayload;
import com.solydshop.ecommerce.payload.response.QuoteDTO;
import com.solydshop.ecommerce.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping("/api/quotes")
    public ResponseEntity<QuoteDTO> submit(
            @Valid @RequestBody QuoteRequestPayload payload,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quoteService.submitQuote(auth.getName(), payload));
    }

    @GetMapping("/api/quotes/my")
    public ResponseEntity<List<QuoteDTO>> myQuotes(Authentication auth) {
        return ResponseEntity.ok(quoteService.getBuyerQuotes(auth.getName()));
    }

    @GetMapping("/api/seller/quotes")
    public ResponseEntity<List<QuoteDTO>> sellerQuotes(Authentication auth) {
        return ResponseEntity.ok(quoteService.getSellerQuotes(auth.getName()));
    }

    @PutMapping("/api/seller/quotes/{id}/respond")
    public ResponseEntity<QuoteDTO> respond(
            @PathVariable Long id,
            @RequestBody QuoteRespondPayload payload,
            Authentication auth) {
        return ResponseEntity.ok(quoteService.respondToQuote(id, auth.getName(), payload));
    }

    @GetMapping("/api/admin/quotes")
    public ResponseEntity<List<QuoteDTO>> adminQuotes() {
        return ResponseEntity.ok(quoteService.getAllQuotes());
    }
}
