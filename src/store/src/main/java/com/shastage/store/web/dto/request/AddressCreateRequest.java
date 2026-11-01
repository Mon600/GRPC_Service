package com.shastage.store.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные адреса")
public record AddressCreateRequest(
        @Schema(description = "Страна", example = "Россия")
        @NotBlank(message = "Country is required")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        String country,

        @Schema(description = "Город", example = "Екатеринбург")
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String city,

        @Schema(description = "Улица и дом", example = "ул. Малышева, 51")
        @NotBlank(message = "Street is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String street
) {
}
