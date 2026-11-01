package com.shastage.store.web.mapper;

import com.shastage.store.domain.command.ProductCreateCommand;
import com.shastage.store.domain.model.Product;
import com.shastage.store.web.dto.request.ProductCreateRequest;
import com.shastage.store.web.dto.response.ProductDetailsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {
    ProductDetailsResponse toProductDetailsResponse(Product product);
    ProductCreateCommand toProductCreateCommand(ProductCreateRequest request);
}
