package com.ecommerce.repository.impl;

import com.ecommerce.model.Address;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.OrderRepository;
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

public class FirestoreOrderRepository implements OrderRepository {

    private static final String COLLECTION_NAME = "orders";
    private final CollectionReference collection;

    public FirestoreOrderRepository(Firestore db) {
        if (db == null) {
            throw new IllegalArgumentException("Firestore instance cannot be null");
        }
        this.collection = db.collection(COLLECTION_NAME);
    }

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("customerId", order.getCustomerId());
        data.put("status", order.getStatus().name());
        data.put("subtotalAmount", order.getSubtotalAmount().toPlainString());
        data.put("discountAmount", order.getDiscountAmount().toPlainString());
        data.put("finalAmount", order.getFinalAmount().toPlainString());
        data.put("couponCode", order.getCouponCode());
        data.put("orderDate", order.getOrderDate().toString());

        // Serialize shipping address
        Address addr = order.getShippingAddress();
        Map<String, String> addrMap = new HashMap<>();
        addrMap.put("street", addr.getStreet());
        addrMap.put("city", addr.getCity());
        addrMap.put("state", addr.getState());
        addrMap.put("zipCode", addr.getZipCode());
        data.put("shippingAddress", addrMap);

        // Serialize order items snapshot
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("productName", item.getProductName());
            itemMap.put("unitPrice", item.getUnitPrice().toPlainString());
            itemMap.put("quantity", item.getQuantity());
            itemsList.add(itemMap);
        }
        data.put("items", itemsList);

        try {
            collection.document(order.getOrderId()).set(data).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save order to Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            DocumentSnapshot doc = collection.document(id.trim()).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(mapToOrder(doc));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read order from Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Order> findAll() {
        try {
            List<QueryDocumentSnapshot> docs = collection.get().get().getDocuments();
            List<Order> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                list.add(mapToOrder(doc));
            }
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch all orders: " + e.getMessage(), e);
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
            throw new RuntimeException("Failed to delete order: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return List.of();
        }
        try {
            List<QueryDocumentSnapshot> docs = collection.whereEqualTo("customerId", customerId.trim()).get().get().getDocuments();
            List<Order> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                list.add(mapToOrder(doc));
            }
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch customer orders: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            return List.of();
        }
        try {
            List<QueryDocumentSnapshot> docs = collection.whereEqualTo("status", status.name()).get().get().getDocuments();
            List<Order> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                list.add(mapToOrder(doc));
            }
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to query orders by status: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Order mapToOrder(DocumentSnapshot doc) {
        String orderId = doc.getString("orderId");
        String customerId = doc.getString("customerId");

        Map<String, String> addrMap = (Map<String, String>) doc.get("shippingAddress");
        Address address = new Address(
                addrMap.get("street"),
                addrMap.get("city"),
                addrMap.get("state"),
                addrMap.get("zipCode")
        );

        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) doc.get("items");
        List<OrderItem> items = new ArrayList<>();
        if (itemsData != null) {
            for (Map<String, Object> itemMap : itemsData) {
                String productId = (String) itemMap.get("productId");
                String productName = (String) itemMap.get("productName");
                BigDecimal unitPrice = new BigDecimal((String) itemMap.get("unitPrice"));
                Long qtyLong = (Long) itemMap.get("quantity");
                int qty = (qtyLong != null) ? qtyLong.intValue() : 1;

                items.add(new OrderItem(productId, productName, unitPrice, qty));
            }
        }

        // Reconstruct Order (using coupon null because discount is already calculated & saved in finalAmount)
        Order order = new Order(orderId, customerId, address, items, null);

        // Synchronize persisted status
        String statusStr = doc.getString("status");
        if (statusStr != null) {
            OrderStatus persistedStatus = OrderStatus.valueOf(statusStr);
            if (persistedStatus == OrderStatus.CONFIRMED) {
                order.confirm();
            } else if (persistedStatus == OrderStatus.SHIPPED) {
                order.confirm();
                order.markShipped();
            } else if (persistedStatus == OrderStatus.DELIVERED) {
                order.confirm();
                order.markShipped();
                order.markDelivered();
            } else if (persistedStatus == OrderStatus.CANCELLED) {
                order.cancel();
            }
        }

        return order;
    }
}
