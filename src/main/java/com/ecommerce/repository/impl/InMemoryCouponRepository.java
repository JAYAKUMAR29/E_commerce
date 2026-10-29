package com.ecommerce.repository.impl;

import com.ecommerce.model.Coupon;
import com.ecommerce.repository.CouponRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCouponRepository implements CouponRepository {

    private final Map<String, Coupon> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Coupon coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon cannot be null");
        }
        storage.put(coupon.getCode().toUpperCase(), coupon);
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
        return Optional.ofNullable(storage.get(code.trim().toUpperCase()));
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id.trim().toUpperCase()) != null;
    }

    @Override
    public boolean existsById(String id) {
        return id != null && storage.containsKey(id.trim().toUpperCase());
    }
}
