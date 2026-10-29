package com.ecommerce.model;

import java.math.BigDecimal;

public class OrderItem {

    private final String productId;
    private final String productName;
    private final BigDecimal unitPrice; // Snapshot of price at checkout time
    private final int quantity;

    public OrderItem(String productId, String productName, BigDecimal unitPrice, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be empty");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product Name cannot be empty");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        this.productId = productId.trim();
        this.productName = productName.trim();
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return String.format("OrderItem[%s (%s) x %d @ ₹%s = ₹%s]",
                productName, productId, quantity, unitPrice, getSubtotal());
    }
}
