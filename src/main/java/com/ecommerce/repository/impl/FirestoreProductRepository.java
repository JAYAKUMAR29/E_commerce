package com.ecommerce.repository.impl;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class FirestoreProductRepository implements ProductRepository {

    private static final String COLLECTION_NAME = "products";
    private final CollectionReference collection;

    public FirestoreProductRepository(Firestore db) {
        if (db == null) {
            throw new IllegalArgumentException("Firestore instance cannot be null");
        }
        this.collection = db.collection(COLLECTION_NAME);
    }

    @Override
    public void save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", product.getId());
        data.put("name", product.getName());
        data.put("category", product.getCategory().name());
        data.put("sellerId", product.getSellerId());
        data.put("price", product.getPrice().toPlainString());
        data.put("stock", product.getStock());
        data.put("active", product.isActive());

        try {
            collection.document(product.getId()).set(data).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save product to Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Product> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        try {
            DocumentSnapshot doc = collection.document(id.trim()).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(mapToProduct(doc));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read product from Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> findAll() {
        try {
            List<QueryDocumentSnapshot> docs = collection.get().get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                products.add(mapToProduct(doc));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch all products from Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        try {
            if (!existsById(id)) {
                return false;
            }
            collection.document(id.trim()).delete().get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete product from Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public List<Product> findByCategory(Category category) {
        if (category == null) {
            return List.of();
        }
        try {
            List<QueryDocumentSnapshot> docs = collection.whereEqualTo("category", category.name()).get().get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                products.add(mapToProduct(doc));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to query products by category: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        // Client-side case-insensitive filter across products for flexible text search
        String lowerKeyword = keyword.trim().toLowerCase();
        return findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    @Override
    public List<Product> findBySellerId(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            return List.of();
        }
        try {
            List<QueryDocumentSnapshot> docs = collection.whereEqualTo("sellerId", sellerId.trim()).get().get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                products.add(mapToProduct(doc));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to query products by seller: " + e.getMessage(), e);
        }
    }

    private Product mapToProduct(DocumentSnapshot doc) {
        String id = doc.getString("id");
        String name = doc.getString("name");
        Category category = Category.valueOf(doc.getString("category"));
        String sellerId = doc.getString("sellerId");
        BigDecimal price = new BigDecimal(doc.getString("price"));
        Long stockLong = doc.getLong("stock");
        int stock = (stockLong != null) ? stockLong.intValue() : 0;

        return new Product(id, name, category, sellerId, price, stock);
    }
}
