package com.ecommerce.model;

import java.math.BigDecimal;

public abstract class Coupon {

    private final String code;
    private final BigDecimal minOrderAmount;
    private boolean active;

    public Coupon(String code, BigDecimal minOrderAmount) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }
        if (minOrderAmount == null || minOrderAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum order amount cannot be negative");
        }
        this.code = code.trim().toUpperCase();
        this.minOrderAmount = minOrderAmount;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Common business rule: Check if order meets minimum threshold and coupon is active
    public boolean isApplicable(BigDecimal orderAmount) {
        if (!active) {
            return false;
        }
        if (orderAmount == null) {
            return false;
        }
        return orderAmount.compareTo(minOrderAmount) >= 0;
    }

    // Abstraction & Polymorphism: Subclasses must provide their own discount calculation algorithm
    public abstract BigDecimal calculateDiscount(BigDecimal orderAmount);
}
