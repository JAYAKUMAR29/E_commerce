package com.ecommerce.repository;

import com.ecommerce.model.Coupon;

import java.util.Optional;

public interface CouponRepository extends CrudRepository<Coupon, String> {

    Optional<Coupon> findByCode(String code);
}
