package com.shastage.store.web.mapper;

import com.shastage.store.domain.command.ImageCreateCommand;
import com.shastage.store.domain.model.Image;
import com.shastage.store.web.dto.request.ImageCreateRequest;
import com.shastage.store.web.dto.response.ImageDetailsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageWebMapper {
    ImageCreateCommand toImageCreateCommand(ImageCreateRequest request);
    ImageDetailsResponse toImageDetailsResponse(Image image);
}
