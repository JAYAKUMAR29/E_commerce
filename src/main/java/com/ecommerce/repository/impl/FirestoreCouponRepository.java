package com.ecommerce.repository.impl;

import com.ecommerce.model.Coupon;
import com.ecommerce.model.FlatDiscountCoupon;
import com.ecommerce.model.PercentageCoupon;
import com.ecommerce.repository.CouponRepository;
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

public class FirestoreCouponRepository implements CouponRepository {

    private static final String COLLECTION_NAME = "coupons";
    private final CollectionReference collection;

    public FirestoreCouponRepository(Firestore db) {
        if (db == null) {
            throw new IllegalArgumentException("Firestore instance cannot be null");
        }
        this.collection = db.collection(COLLECTION_NAME);
    }

    @Override
    public void save(Coupon coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon cannot be null");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("code", coupon.getCode());
        data.put("minOrderAmount", coupon.getMinOrderAmount().toPlainString());
        data.put("active", coupon.isActive());

        if (coupon instanceof PercentageCoupon pc) {
            data.put("type", "PERCENTAGE");
            data.put("percentage", pc.getPercentage());
            data.put("maxDiscount", pc.getMaxDiscount() != null ? pc.getMaxDiscount().toPlainString() : null);
        } else if (coupon instanceof FlatDiscountCoupon fc) {
            data.put("type", "FLAT");
            data.put("discountAmount", fc.getDiscountAmount().toPlainString());
        }

        try {
            collection.document(coupon.getCode()).set(data).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save coupon to Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Coupon> findById(String id) {
        return findByCode(id);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        try {
            DocumentSnapshot doc = collection.document(code.trim().toUpperCase()).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(mapToCoupon(doc));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read coupon from Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Coupon> findAll() {
        try {
            List<QueryDocumentSnapshot> docs = collection.get().get().getDocuments();
            List<Coupon> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                list.add(mapToCoupon(doc));
            }
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch all coupons: " + e.getMessage(), e);
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
            collection.document(id.trim().toUpperCase()).delete().get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete coupon: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    private Coupon mapToCoupon(DocumentSnapshot doc) {
        String code = doc.getString("code");
        BigDecimal minOrder = new BigDecimal(doc.getString("minOrderAmount"));
        String type = doc.getString("type");

        Coupon coupon;
        if ("PERCENTAGE".equalsIgnoreCase(type)) {
            Double percentage = doc.getDouble("percentage");
            String maxDiscountStr = doc.getString("maxDiscount");
            BigDecimal maxDiscount = (maxDiscountStr != null) ? new BigDecimal(maxDiscountStr) : null;
            coupon = new PercentageCoupon(code, minOrder, percentage != null ? percentage : 0.0, maxDiscount);
        } else {
            BigDecimal discountAmount = new BigDecimal(doc.getString("discountAmount"));
            coupon = new FlatDiscountCoupon(code, minOrder, discountAmount);
        }

        Boolean active = doc.getBoolean("active");
        if (active != null) {
            coupon.setActive(active);
        }

        return coupon;
    }
}
