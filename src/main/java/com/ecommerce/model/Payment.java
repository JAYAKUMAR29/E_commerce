package com.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private final String paymentId;
    private final String orderId;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final LocalDateTime createdAt;

    private PaymentStatus status;
    private String transactionId;
    private String failureReason;

    public Payment(String paymentId, String orderId, BigDecimal amount, PaymentMethod method) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be empty");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        this.paymentId = paymentId.trim();
        this.orderId = orderId.trim();
        this.amount = amount;
        this.method = method;
        this.createdAt = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Mark payment as successfully processed
    public void markSuccess(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be marked as SUCCESS");
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID is required for successful payment");
        }
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId.trim();
    }

    // Mark payment as failed
    public void markFailed(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be marked as FAILED");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = (reason == null || reason.isBlank()) ? "Unknown payment failure" : reason.trim();
    }

    // Mark payment as refunded (e.g. after customer returns item)
    public void markRefunded() {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only SUCCESSFUL payments can be REFUNDED");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    @Override
    public String toString() {
        return String.format("Payment[ID=%s, OrderID=%s, Amount=₹%s, Method=%s, Status=%s, TxnID=%s]",
                paymentId, orderId, amount, method, status, transactionId != null ? transactionId : "N/A");
    }
}
