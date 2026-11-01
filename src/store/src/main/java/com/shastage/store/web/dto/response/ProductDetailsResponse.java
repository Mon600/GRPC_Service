package com.shastage.store.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Полные данные товара")
public record ProductDetailsResponse(
        @Schema(description = "Идентификатор товара", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "Название товара", example = "Холодильник ATLANT ХМ 4425-009 ND")
        String name,
        @Schema(description = "Категория товара", example = "Крупная бытовая техника")
        String category,
        @Schema(description = "Цена за единицу", example = "59990.00")
        BigDecimal price,
        @Schema(description = "Доступное количество", example = "47")
        Integer availableStock,
        @Schema(description = "Дата и время последней закупки", type = "string", format = "date-time", example = "2026-10-01T06:30:00Z")
        Instant lastUpdateDate,
        @Schema(description = "Идентификатор поставщика", example = "7d444840-9dc0-11d1-b245-5ffdce74fad2")
        UUID supplierId,
        @Schema(description = "Идентификатор изображения; null после удаления изображения", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
        UUID imageId) {
}
