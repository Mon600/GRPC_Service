package com.shastage.store.web.dto.response;

import com.shastage.store.domain.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Полные данные клиента")
public record ClientDetailsResponse(
        @Schema(description = "Идентификатор клиента", example = "a3bb189e-8bf9-3888-9912-ace4e6543002")
        UUID id,
        @Schema(description = "Имя", example = "Иван")
        String name,
        @Schema(description = "Фамилия", example = "Иванов")
        String surname,
        @Schema(description = "Дата рождения", type = "string", format = "date", example = "1994-05-17")
        LocalDate birthday,
        @Schema(description = "Пол", example = "MALE", allowableValues = {"MALE", "FEMALE"})
        Gender gender,
        @Schema(description = "Дата регистрации", type = "string", format = "date", example = "2026-10-01")
        LocalDate registrationDate,
        @Schema(description = "Идентификатор адреса", example = "54e2dfe4-4dd8-4f7e-9f9a-5c9bdb3b01f6")
        UUID addressId) {
}
