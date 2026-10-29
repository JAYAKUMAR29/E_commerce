package com.ecommerce.model;

import java.math.BigDecimal;

public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (quantity > product.getStock()) {
            throw new IllegalStateException("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStock() + ")");
        }

        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    // Calculates item subtotal: price * quantity
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    // Updates to an absolute quantity (e.g., user types '3' in quantity box)
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (newQuantity > product.getStock()) {
            throw new IllegalStateException("Requested quantity (" + newQuantity + ") exceeds available stock (" + product.getStock() + ")");
        }
        this.quantity = newQuantity;
    }

    // Increments by additional units (e.g., user clicks '+' button)
    public void addQuantity(int extraQuantity) {
        if (extraQuantity <= 0) {
            throw new IllegalArgumentException("Extra quantity must be greater than zero");
        }
        int total = this.quantity + extraQuantity;
        if (total > product.getStock()) {
            throw new IllegalStateException("Total requested quantity (" + total + ") exceeds available stock (" + product.getStock() + ")");
        }
        this.quantity = total;
    }

    @Override
    public String toString() {
        return String.format("CartItem[Product=%s, Qty=%d, UnitPrice=%s, Subtotal=%s]",
                product.getName(), quantity, product.getPrice(), getSubtotal());
    }
}
