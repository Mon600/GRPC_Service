package com.shastage.store.web.dto.request;

import com.shastage.store.domain.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Данные для создания клиента")
public record ClientCreateRequest(
        @Schema(description = "Имя", example = "Иван")
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Schema(description = "Фамилия", example = "Иванов")
        @NotBlank(message = "Surname is required.")
        @Size(max = 100, message = "Surname must not exceed 100 characters")
        String surname,

        @Schema(description = "Дата рождения", type = "string", format = "date", example = "1994-05-17")
        @NotNull(message = "Birthday is required.")
        LocalDate birthday,

        @Schema(description = "Пол", example = "MALE", allowableValues = {"MALE", "FEMALE"})
        @NotNull(message = "Gender is required.")
        Gender gender,

        @Schema(description = "Дата регистрации", type = "string", format = "date", example = "2026-10-01")
        @NotNull(message = "Registration date is required.")
        LocalDate registrationDate,

        @Schema(
                description = "Идентификатор существующего адреса",
                example = "54e2dfe4-4dd8-4f7e-9f9a-5c9bdb3b01f6"
        )
        @NotNull(message = "Address ID is required.")
        UUID addressId) {
}
