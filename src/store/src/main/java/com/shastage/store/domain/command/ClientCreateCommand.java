package com.shastage.store.domain.command;

import com.shastage.store.domain.model.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record ClientCreateCommand(
        String name,
        String surname,
        LocalDate birthday,
        Gender gender,
        LocalDate registrationDate,
        UUID addressId) {
}
