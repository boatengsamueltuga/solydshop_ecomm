package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.RejectRequest;
import com.solydshop.ecommerce.payload.request.SellerDowngradeRequestPayload;
import com.solydshop.ecommerce.payload.response.SellerDowngradeRequestDTO;
import com.solydshop.ecommerce.service.SellerDowngradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SellerDowngradeController {

    private final SellerDowngradeService service;

    public SellerDowngradeController(SellerDowngradeService service) {
        this.service = service;
    }

    /** Seller requests reverting to a buyer account */
    @PostMapping("/api/seller-downgrade-requests")
    public ResponseEntity<SellerDowngradeRequestDTO> submit(
            @Valid @RequestBody SellerDowngradeRequestPayload payload,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.submit(auth.getName(), payload));
    }

    /** Seller retrieves their own most recent downgrade request */
    @GetMapping("/api/seller-downgrade-requests/my")
    public ResponseEntity<SellerDowngradeRequestDTO> getMyRequest(Authentication auth) {
        return ResponseEntity.ok(service.getMyRequest(auth.getName()));
    }

    /** Admin retrieves all downgrade requests */
    @GetMapping("/api/admin/seller-downgrade-requests")
    public ResponseEntity<List<SellerDowngradeRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    /** Admin approves a downgrade request and reverts the user to a buyer */
    @PostMapping("/api/admin/seller-downgrade-requests/{id}/approve")
    public ResponseEntity<SellerDowngradeRequestDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    /** Admin rejects a downgrade request with a reason */
    @PostMapping("/api/admin/seller-downgrade-requests/{id}/reject")
    public ResponseEntity<SellerDowngradeRequestDTO> reject(
            @PathVariable Long id,
            @RequestBody RejectRequest body) {
        return ResponseEntity.ok(service.reject(id, body.getReason()));
    }
}
