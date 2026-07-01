package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.RejectRequest;
import com.solydshop.ecommerce.payload.request.SellerApplicationRequest;
import com.solydshop.ecommerce.payload.response.SellerApplicationDTO;
import com.solydshop.ecommerce.service.SellerApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SellerApplicationController {

    private final SellerApplicationService service;

    public SellerApplicationController(SellerApplicationService service) {
        this.service = service;
    }

    /** Buyer submits a new seller application */
    @PostMapping("/api/seller-applications")
    public ResponseEntity<SellerApplicationDTO> submit(
            @Valid @RequestBody SellerApplicationRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.submit(auth.getName(), request));
    }

    /** Buyer retrieves their own most recent application */
    @GetMapping("/api/seller-applications/my")
    public ResponseEntity<SellerApplicationDTO> getMyApplication(Authentication auth) {
        return ResponseEntity.ok(service.getMyApplication(auth.getName()));
    }

    /** Admin retrieves all applications */
    @GetMapping("/api/admin/seller-applications")
    public ResponseEntity<List<SellerApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    /** Admin approves an application and grants ROLE_SELLER */
    @PostMapping("/api/admin/seller-applications/{id}/approve")
    public ResponseEntity<SellerApplicationDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    /** Admin rejects an application with a reason */
    @PostMapping("/api/admin/seller-applications/{id}/reject")
    public ResponseEntity<SellerApplicationDTO> reject(
            @PathVariable Long id,
            @RequestBody RejectRequest body) {
        return ResponseEntity.ok(service.reject(id, body.getReason()));
    }
}
