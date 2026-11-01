package com.shastage.store.domain.repository;

import com.shastage.store.domain.model.Address;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository {
    void save(Address address);
    boolean existsById(UUID id);
    Optional<Address> findById(UUID id);
}
