package com.shastage.store.datasource.mapper;

import com.shastage.store.datasource.entity.SupplierEntity;
import com.shastage.store.domain.model.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierEntityMapper {
    Supplier toDomain(SupplierEntity entity);
    SupplierEntity toEntity(Supplier supplier);
}
