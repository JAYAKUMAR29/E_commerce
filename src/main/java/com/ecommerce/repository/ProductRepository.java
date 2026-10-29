package com.ecommerce.repository;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;

import java.util.List;

public interface ProductRepository extends CrudRepository<Product, String> {

    List<Product> findByCategory(Category category);

    List<Product> searchByName(String keyword);

    List<Product> findBySellerId(String sellerId);
}
