package com.shastage.store.datasource.repository.jpa;

import com.shastage.store.datasource.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageJpaRepository extends JpaRepository<ImageEntity, UUID> {
}
