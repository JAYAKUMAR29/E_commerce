package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {

    void save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);

    boolean existsById(ID id);
}
