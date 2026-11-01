package com.shastage.store.datasource.repository;

import com.shastage.store.datasource.entity.AddressEntity;
import com.shastage.store.datasource.mapper.AddressEntityMapper;
import com.shastage.store.datasource.repository.jpa.AddressJpaRepository;
import com.shastage.store.domain.model.Address;
import com.shastage.store.domain.repository.AddressRepository;
import com.shastage.store.web.mapper.AddressWebMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class AddressRepositoryAdapter implements AddressRepository {

    AddressJpaRepository addressJpaRepository;
    AddressEntityMapper addressEntityMapper;

    public boolean existsById(UUID id) {
        return addressJpaRepository.existsById(id);
    }

    public void save(Address address) {
        AddressEntity entity = addressEntityMapper.toEntity(address);
        addressJpaRepository.save(entity);
    }

    public Optional<Address> findById(UUID id) {
        return addressJpaRepository.findById(id).map(addressEntityMapper::toDomain);
    }
}
