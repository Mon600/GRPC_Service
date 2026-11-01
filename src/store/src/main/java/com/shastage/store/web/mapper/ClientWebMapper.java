package com.shastage.store.web.mapper;

import com.shastage.store.domain.command.ClientCreateCommand;
import com.shastage.store.domain.model.Client;
import com.shastage.store.web.dto.request.ClientCreateRequest;
import com.shastage.store.web.dto.response.ClientDetailsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientWebMapper {
    ClientDetailsResponse toClientDetailsResponse(Client client);
    ClientCreateCommand toClientCreateCommand(ClientCreateRequest clientCreateRequest);
}
