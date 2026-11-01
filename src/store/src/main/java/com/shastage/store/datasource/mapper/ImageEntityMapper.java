package com.shastage.store.datasource.mapper;

import com.shastage.store.datasource.entity.ImageEntity;
import com.shastage.store.domain.model.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageEntityMapper {
    Image toDomain(ImageEntity entity);
    ImageEntity toEntity(Image image);
}
