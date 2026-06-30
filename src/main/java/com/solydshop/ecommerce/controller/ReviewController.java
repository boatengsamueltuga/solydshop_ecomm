package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.ReviewRequest;
import com.solydshop.ecommerce.payload.response.ReviewResponse;
import com.solydshop.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/public/reviews/product/{productId}")
    public ResponseEntity<ReviewResponse> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsForProduct(productId));
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<ReviewResponse.ReviewDTO> submitReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        ReviewResponse.ReviewDTO dto = reviewService.submitReview(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
