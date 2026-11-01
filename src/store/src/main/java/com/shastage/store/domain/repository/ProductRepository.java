package com.shastage.store.domain.repository;

import com.shastage.store.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    List<Product> findAll();
    void save(Product product);
    void deleteById(UUID id);
}
