package com.shastage.store.datasource.mapper;

import com.shastage.store.datasource.entity.AddressEntity;
import com.shastage.store.domain.model.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressEntityMapper {
    Address toDomain(AddressEntity entity);
    AddressEntity toEntity(Address address);
}
