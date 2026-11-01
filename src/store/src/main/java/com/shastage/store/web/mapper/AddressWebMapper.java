package com.shastage.store.web.mapper;

import com.shastage.store.domain.command.AddressPutCommand;
import com.shastage.store.web.dto.request.AddressCreateRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressWebMapper {
    AddressPutCommand toAddressPutCommand(AddressCreateRequest addressCreateRequest);
}
