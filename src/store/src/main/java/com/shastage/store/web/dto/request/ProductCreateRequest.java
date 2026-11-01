package com.shastage.store.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Данные для создания товара")
public record ProductCreateRequest(
        @Schema(description = "Название товара", example = "Холодильник ATLANT ХМ 4425-009 ND")
        @NotBlank
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Schema(description = "Категория товара", example = "Крупная бытовая техника")
        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        @Schema(description = "Цена за единицу", example = "59990.00")
        @NotNull(message = "Price is required")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
        @Positive
        BigDecimal price,

        @Schema(description = "Доступное количество", example = "50", minimum = "1")
        @NotNull(message = "Available stock is required")
        @Positive
        Integer availableStock,

        @Schema(description = "Идентификатор поставщика", example = "7d444840-9dc0-11d1-b245-5ffdce74fad2")
        @NotNull
        UUID supplierId,

        @Schema(description = "Идентификатор изображения", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
        @NotNull
        UUID imageId) {
}
