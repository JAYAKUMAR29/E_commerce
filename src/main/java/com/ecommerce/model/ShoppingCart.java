package com.ecommerce.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShoppingCart {

    private final String customerId;
    // Map with Product ID as key for O(1) lookup, LinkedHashMap preserves insertion order
    private final Map<String, CartItem> items;

    public ShoppingCart(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        this.customerId = customerId.trim();
        this.items = new LinkedHashMap<>();
    }

    public String getCustomerId() {
        return customerId;
    }

    // Add product to cart. If already present, increment quantity.
    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (items.containsKey(product.getId())) {
            CartItem existingItem = items.get(product.getId());
            existingItem.addQuantity(quantity);
        } else {
            CartItem newItem = new CartItem(product, quantity);
            items.put(product.getId(), newItem);
        }
    }

    // Remove product completely from cart
    public void removeItem(String productId) {
        if (productId == null || !items.containsKey(productId)) {
            throw new IllegalArgumentException("Product not found in cart");
        }
        items.remove(productId);
    }

    // Update quantity of an item already in the cart
    public void updateQuantity(String productId, int newQuantity) {
        if (productId == null || !items.containsKey(productId)) {
            throw new IllegalArgumentException("Product not found in cart");
        }
        items.get(productId).updateQuantity(newQuantity);
    }

    // Calculate sum total of all items in cart
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items.values()) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public Collection<CartItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    @Override
    public String toString() {
        return String.format("ShoppingCart[Customer=%s, TotalItems=%d, TotalAmount=%s]",
                customerId, items.size(), getTotalAmount());
    }
}
