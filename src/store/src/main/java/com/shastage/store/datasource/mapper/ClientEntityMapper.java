package com.shastage.store.datasource.mapper;

import com.shastage.store.datasource.entity.ClientEntity;
import com.shastage.store.domain.model.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientEntityMapper {
    ClientEntity toEntity(Client client);
    Client toDomain(ClientEntity entity);
}
