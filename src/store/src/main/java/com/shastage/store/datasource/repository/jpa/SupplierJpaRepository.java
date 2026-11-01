package com.shastage.store.datasource.repository.jpa;

import com.shastage.store.datasource.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, UUID> {
}
