package com.shastage.store.domain.repository;

import com.shastage.store.domain.model.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository {
    List<Supplier> findAll();
    Optional<Supplier> findById(UUID id);
    void deleteById(UUID id);
    void save(Supplier supplier);
}
