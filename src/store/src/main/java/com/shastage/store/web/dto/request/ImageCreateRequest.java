package com.shastage.store.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Данные для загрузки изображения товара")
public record ImageCreateRequest(
        @Schema(
                description = "Содержимое изображения в Base64",
                type = "string",
                format = "byte",
                example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB"
        )
        @NotNull(message = "Image is required")
        @Size(min = 1, message = "Image must not be empty")
        byte[] image,

        @Schema(description = "Идентификатор товара", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Product id is required")
        UUID productId
) {
}
