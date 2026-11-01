package com.shastage.store.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Количество товара для списания")
public record ProductStockDecreaseRequest(
        @Schema(description = "Списываемое количество", example = "3", minimum = "1")
        @NotNull
        @Positive
        Integer decreaseBy
) {
}
