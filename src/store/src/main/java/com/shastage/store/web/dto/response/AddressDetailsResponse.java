package com.shastage.store.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Полные данные адреса")
public record AddressDetailsResponse(
        @Schema(description = "Идентификатор адреса", example = "54e2dfe4-4dd8-4f7e-9f9a-5c9bdb3b01f6")
        UUID id,
        @Schema(description = "Страна", example = "Россия")
        String country,
        @Schema(description = "Город", example = "Екатеринбург")
        String city,
        @Schema(description = "Улица и дом", example = "ул. Малышева, 51")
        String street
) {
}
