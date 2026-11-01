package com.shastage.store.web.mapper;

import com.shastage.store.domain.command.SupplierCreateCommand;
import com.shastage.store.domain.model.Supplier;
import com.shastage.store.web.dto.request.SupplierCreateRequest;
import com.shastage.store.web.dto.response.SupplierDetailsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierWebMapper {
    SupplierDetailsResponse toSupplierDetailsResponse(Supplier supplier);
    SupplierCreateCommand toSupplierCreateCommand(SupplierCreateRequest request);
}
