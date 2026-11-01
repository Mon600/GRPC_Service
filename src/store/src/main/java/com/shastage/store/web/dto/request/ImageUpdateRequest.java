package com.shastage.store.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Новое содержимое изображения")
public record ImageUpdateRequest(
        @Schema(
                description = "Содержимое изображения в Base64",
                type = "string",
                format = "byte",
                example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB"
        )
        @NotNull(message = "Image is required")
        @Size(min = 1, message = "Image must not be empty")
        byte[] image
) {
}
