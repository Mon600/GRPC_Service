package com.shastage.store.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Полные данные поставщика")
public record SupplierDetailsResponse(
        @Schema(description = "Идентификатор поставщика", example = "7d444840-9dc0-11d1-b245-5ffdce74fad2")
        UUID id,
        @Schema(description = "Название поставщика", example = "ООО ТехноПоставка")
        String name,
        @Schema(description = "Идентификатор адреса", example = "54e2dfe4-4dd8-4f7e-9f9a-5c9bdb3b01f6")
        UUID addressId,
        @Schema(description = "Контактный телефон", example = "+7 912 345-67-89")
        String phoneNumber) {
}
