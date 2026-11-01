package com.shastage.store.web.mapper;

import app.generated.TokenResponse;
import com.shastage.store.web.dto.response.LoginResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthWebMapper {
    LoginResponse toLoginResponse(TokenResponse tokenResponse);
}
