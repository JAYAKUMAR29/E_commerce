package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.repository.ProductRepository;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class CartService {

    private final ProductRepository productRepository;
    // In-memory active cart per customer: customerId -> ShoppingCart
    private final Map<String, ShoppingCart> activeCarts = new ConcurrentHashMap<>();

    public CartService(ProductRepository productRepository) {
        if (productRepository == null) {
            throw new IllegalArgumentException("Product repository cannot be null");
        }
        this.productRepository = productRepository;
    }

    // Retrieves existing cart or lazily initializes a new cart for customer
    public ShoppingCart getCart(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        return activeCarts.computeIfAbsent(customerId.trim(), ShoppingCart::new);
    }

    public void addItemToCart(String customerId, String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));

        if (!product.isActive()) {
            throw new IllegalStateException("Product is currently inactive: " + product.getName());
        }

        ShoppingCart cart = getCart(customerId);
        cart.addItem(product, quantity);
    }

    public void updateItemQuantity(String customerId, String productId, int quantity) {
        ShoppingCart cart = getCart(customerId);
        cart.updateQuantity(productId, quantity);
    }

    public void removeItemFromCart(String customerId, String productId) {
        ShoppingCart cart = getCart(customerId);
        cart.removeItem(productId);
    }

    public void clearCart(String customerId) {
        ShoppingCart cart = getCart(customerId);
        cart.clear();
    }
}
