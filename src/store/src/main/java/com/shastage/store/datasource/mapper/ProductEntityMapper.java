package com.shastage.store.datasource.mapper;

import com.shastage.store.datasource.entity.ProductEntity;
import com.shastage.store.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductEntityMapper {
    Product toDomain(ProductEntity entity);
    ProductEntity toEntity(Product client);
}
