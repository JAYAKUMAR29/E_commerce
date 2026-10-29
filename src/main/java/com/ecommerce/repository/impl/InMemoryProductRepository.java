package com.ecommerce.repository.impl;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryProductRepository implements ProductRepository {

    // Thread-safe in-memory storage mapping Product ID -> Product
    private final Map<String, Product> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        storage.put(product.getId(), product);
    }

    @Override
    public Optional<Product> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsById(String id) {
        return id != null && storage.containsKey(id);
    }

    @Override
    public List<Product> findByCategory(Category category) {
        if (category == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(p -> p.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return storage.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findBySellerId(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            return List.of();
        }
        return storage.values().stream()
                .filter(p -> p.getSellerId().equalsIgnoreCase(sellerId.trim()))
                .collect(Collectors.toList());
    }
}
