package com.ecommerce.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageCoupon extends Coupon {

    private final double percentage;
    private final BigDecimal maxDiscount; // null indicates uncapped discount

    public PercentageCoupon(String code, BigDecimal minOrderAmount, double percentage, BigDecimal maxDiscount) {
        super(code, minOrderAmount);

        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 1 and 100");
        }
        if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max discount must be greater than zero");
        }

        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
    }

    public double getPercentage() {
        return percentage;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isApplicable(orderAmount)) {
            return BigDecimal.ZERO;
        }

        // discount = (orderAmount * percentage) / 100
        BigDecimal calculated = orderAmount.multiply(BigDecimal.valueOf(percentage))
                                           .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Cap at maxDiscount if defined
        if (maxDiscount != null && calculated.compareTo(maxDiscount) > 0) {
            return maxDiscount;
        }

        return calculated;
    }

    @Override
    public String toString() {
        return String.format("PercentageCoupon[Code=%s, Discount=%.1f%%, MinOrder=₹%s, MaxCap=%s]",
                getCode(), percentage, getMinOrderAmount(), maxDiscount != null ? "₹" + maxDiscount : "None");
    }
}
