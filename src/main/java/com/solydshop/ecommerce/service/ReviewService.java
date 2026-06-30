package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.ReviewRequest;
import com.solydshop.ecommerce.payload.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse getReviewsForProduct(Long productId);
    ReviewResponse.ReviewDTO submitReview(String email, ReviewRequest request);
}
