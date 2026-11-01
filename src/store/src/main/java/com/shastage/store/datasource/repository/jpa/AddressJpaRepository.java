package com.shastage.store.datasource.repository.jpa;

import com.shastage.store.datasource.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<AddressEntity, UUID> {
}
