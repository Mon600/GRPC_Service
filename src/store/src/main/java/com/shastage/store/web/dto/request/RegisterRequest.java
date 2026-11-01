package com.shastage.store.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 2, max = 255)
        String name,

        @NotBlank
        @Size(min = 2, max = 255)
        String surname,

        @NotBlank
        String number,

        @NotNull
        @Size(min = 8, max = 255)
        String password
) {
}
