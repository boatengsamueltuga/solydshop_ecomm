package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.Review;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.ReviewRequest;
import com.solydshop.ecommerce.payload.response.ReviewResponse;
import com.solydshop.ecommerce.repository.OrderRepository;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.ReviewRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ProductRepository productRepository,
                             UserRepository userRepository,
                             OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public ReviewResponse getReviewsForProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductProductIdOrderByCreatedAtDesc(productId);
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        double averageRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        List<ReviewResponse.ReviewDTO> dtos = reviews.stream().map(r -> {
            ReviewResponse.ReviewDTO dto = new ReviewResponse.ReviewDTO();
            dto.setReviewId(r.getReviewId());
            dto.setUserId(r.getUser().getUserId());
            dto.setUserName(r.getUser().getName());
            dto.setRating(r.getRating());
            dto.setComment(r.getComment());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setVerifiedPurchase(hasUserPurchasedProduct(r.getUser(), r.getProduct()));
            return dto;
        }).toList();

        return new ReviewResponse(dtos, averageRating);
    }

    @Override
    public ReviewResponse.ReviewDTO submitReview(String email, ReviewRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByUserUserIdAndProductProductId(user.getUserId(), product.getProductId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product.");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review = reviewRepository.save(review);

        ReviewResponse.ReviewDTO dto = new ReviewResponse.ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setVerifiedPurchase(hasUserPurchasedProduct(user, product));
        return dto;
    }

    private boolean hasUserPurchasedProduct(User user, Product product) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .anyMatch(order -> order.getOrderItems().stream()
                        .anyMatch(item -> item.getProduct() != null &&
                                item.getProduct().getProductId().equals(product.getProductId())));
    }
}
