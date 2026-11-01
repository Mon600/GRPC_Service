package com.shastage.store.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Данные загруженного изображения")
public record ImageDetailsResponse(
        @Schema(description = "Идентификатор изображения", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
        UUID id,
        @Schema(description = "Содержимое изображения в Base64", type = "string", format = "byte")
        byte[] image
) {
}
