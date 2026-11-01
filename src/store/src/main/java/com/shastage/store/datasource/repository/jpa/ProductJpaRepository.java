package com.shastage.store.datasource.repository.jpa;

import com.shastage.store.datasource.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
}
