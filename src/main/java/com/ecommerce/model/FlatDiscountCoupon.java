package com.ecommerce.model;

import java.math.BigDecimal;

public class FlatDiscountCoupon extends Coupon {

    private final BigDecimal discountAmount;

    public FlatDiscountCoupon(String code, BigDecimal minOrderAmount, BigDecimal discountAmount) {
        super(code, minOrderAmount);

        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount amount must be greater than zero");
        }

        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isApplicable(orderAmount)) {
            return BigDecimal.ZERO;
        }

        // Safety: Discount cannot exceed the total order amount
        if (discountAmount.compareTo(orderAmount) > 0) {
            return orderAmount;
        }

        return discountAmount;
    }

    @Override
    public String toString() {
        return String.format("FlatDiscountCoupon[Code=%s, Discount=₹%s, MinOrder=₹%s]",
                getCode(), discountAmount, getMinOrderAmount());
    }
}
