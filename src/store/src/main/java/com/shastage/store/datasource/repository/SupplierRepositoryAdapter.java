package com.shastage.store.datasource.repository;

import com.shastage.store.datasource.entity.SupplierEntity;
import com.shastage.store.datasource.mapper.SupplierEntityMapper;
import com.shastage.store.datasource.repository.jpa.SupplierJpaRepository;
import com.shastage.store.domain.model.Supplier;
import com.shastage.store.domain.repository.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Repository
public class SupplierRepositoryAdapter implements SupplierRepository {

    SupplierEntityMapper supplierEntityMapper;
    SupplierJpaRepository supplierJpaRepository;

    public List<Supplier> findAll() {
        List<SupplierEntity> entities = supplierJpaRepository.findAll();
        return entities.stream()
                .map(supplierEntityMapper::toDomain)
                .toList();
    }

    public Optional<Supplier> findById(UUID id) {
        return supplierJpaRepository.findById(id).map(supplierEntityMapper::toDomain);
    }

    public void deleteById(UUID id) {
        supplierJpaRepository.deleteById(id);
    }

    public void save(Supplier supplier) {
        supplierJpaRepository.save(supplierEntityMapper.toEntity(supplier));
    }
}
