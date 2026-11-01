package com.shastage.store.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotNull
        @Size(min = 8, max = 255)
        String oldPassword,

        @NotNull
        @Size(min = 8, max = 255)
        String newPassword
) {
}
