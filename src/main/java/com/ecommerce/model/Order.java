package com.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    private final String orderId;
    private final String customerId;
    private final Address shippingAddress;
    private final List<OrderItem> items;
    private final LocalDateTime orderDate;
    private final String couponCode;

    private final BigDecimal subtotalAmount;
    private final BigDecimal discountAmount;
    private final BigDecimal finalAmount;

    private OrderStatus status;

    public Order(String orderId, String customerId, Address shippingAddress, List<OrderItem> items, Coupon coupon) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        this.orderId = orderId.trim();
        this.customerId = customerId.trim();
        this.shippingAddress = shippingAddress;
        this.items = new ArrayList<>(items);
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PLACED;

        // Calculate subtotal
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            calculatedSubtotal = calculatedSubtotal.add(item.getSubtotal());
        }
        this.subtotalAmount = calculatedSubtotal;

        // Apply coupon if provided and applicable
        if (coupon != null && coupon.isApplicable(this.subtotalAmount)) {
            this.couponCode = coupon.getCode();
            this.discountAmount = coupon.calculateDiscount(this.subtotalAmount);
        } else {
            this.couponCode = null;
            this.discountAmount = BigDecimal.ZERO;
        }

        this.finalAmount = this.subtotalAmount.subtract(this.discountAmount);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    // --- State Machine Transitions (Business Rules) ---

    public void confirm() {
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("Only PLACED orders can be confirmed. Current status: " + status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void markShipped() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED orders can be shipped. Current status: " + status);
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void markDelivered() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only SHIPPED orders can be delivered. Current status: " + status);
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        // Business Rule: Can only cancel if not yet shipped
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that has already been shipped or delivered.");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void requestReturn() {
        // Business Rule: Can only return delivered orders
        if (status != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Return can only be requested for DELIVERED orders.");
        }
        this.status = OrderStatus.RETURN_REQUESTED;
    }

    public void completeReturn() {
        if (status != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("Only orders with RETURN_REQUESTED can be marked as RETURNED.");
        }
        this.status = OrderStatus.RETURNED;
    }

    @Override
    public String toString() {
        return String.format("Order[ID=%s, Customer=%s, Status=%s, Items=%d, Subtotal=₹%s, Discount=₹%s, Final=₹%s]",
                orderId, customerId, status, items.size(), subtotalAmount, discountAmount, finalAmount);
    }
}
