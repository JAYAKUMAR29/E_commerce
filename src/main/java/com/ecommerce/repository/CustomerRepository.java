package com.ecommerce.repository;

import com.ecommerce.model.Customer;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, String> {

    Optional<Customer> findByEmail(String email);
}
