package com.shastage.store.web.dto.response;

public record LoginResponse(
        String token,
        String tokenType
) {
}
