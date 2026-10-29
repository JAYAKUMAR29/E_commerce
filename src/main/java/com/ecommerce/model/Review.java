package com.ecommerce.model;

import java.time.LocalDateTime;

public class Review {

    private final String reviewId;
    private final String productId;
    private final String customerId;
    private final int rating; // 1 to 5 stars
    private final String comment;
    private final LocalDateTime reviewDate;

    public Review(String reviewId, String productId, String customerId, int rating, String comment) {
        if (reviewId == null || reviewId.isBlank()) {
            throw new IllegalArgumentException("Review ID cannot be empty");
        }
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be empty");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Review comment cannot be empty");
        }

        this.reviewId = reviewId.trim();
        this.productId = productId.trim();
        this.customerId = customerId.trim();
        this.rating = rating;
        this.comment = comment.trim();
        this.reviewDate = LocalDateTime.now();
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getProductId() {
        return productId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    @Override
    public String toString() {
        return String.format("Review[%d/5 stars by %s on %s: \"%s\"]",
                rating, customerId, productId, comment);
    }
}
