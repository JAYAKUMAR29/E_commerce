package com.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Refund {

    private final String refundId;
    private final String orderId;
    private final BigDecimal amount;
    private final String reason;
    private final LocalDateTime processedAt;

    public Refund(String refundId, String orderId, BigDecimal amount, String reason) {
        if (refundId == null || refundId.isBlank()) {
            throw new IllegalArgumentException("Refund ID cannot be empty");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Refund reason cannot be empty");
        }

        this.refundId = refundId.trim();
        this.orderId = orderId.trim();
        this.amount = amount;
        this.reason = reason.trim();
        this.processedAt = LocalDateTime.now();
    }

    public String getRefundId() {
        return refundId;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    @Override
    public String toString() {
        return String.format("Refund[ID=%s, Order=%s, Amount=₹%s, Reason=\"%s\", Date=%s]",
                refundId, orderId, amount, reason, processedAt);
    }
}
