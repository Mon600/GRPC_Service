package com.shastage.store.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Данные для создания поставщика")
public record SupplierCreateRequest(
        @Schema(description = "Название поставщика", example = "ООО ТехноПоставка")
        @NotBlank
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Schema(description = "Идентификатор существующего адреса", example = "54e2dfe4-4dd8-4f7e-9f9a-5c9bdb3b01f6")
        @NotNull
        UUID addressId,

        @Schema(description = "Контактный телефон", example = "+7 912 345-67-89")
        @NotBlank
        @Size(max = 30, message = "Name must not exceed 30 characters")
        String phoneNumber) {
}
