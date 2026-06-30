package com.solydshop.ecommerce.payload.response;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponse {

    private List<ReviewDTO> reviews;
    private double averageRating;
    private int totalReviews;

    public ReviewResponse(List<ReviewDTO> reviews, double averageRating) {
        this.reviews = reviews;
        this.averageRating = averageRating;
        this.totalReviews = reviews.size();
    }

    public List<ReviewDTO> getReviews() { return reviews; }
    public double getAverageRating() { return averageRating; }
    public int getTotalReviews() { return totalReviews; }

    public static class ReviewDTO {
        private Long reviewId;
        private Long userId;
        private String userName;
        private int rating;
        private String comment;
        private LocalDateTime createdAt;
        private boolean verifiedPurchase;

        public Long getReviewId() { return reviewId; }
        public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public boolean isVerifiedPurchase() { return verifiedPurchase; }
        public void setVerifiedPurchase(boolean verifiedPurchase) { this.verifiedPurchase = verifiedPurchase; }
    }
}
