package com.shastage.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Client {
    private UUID id;
    private String name;
    private String surname;
    private LocalDate birthday;
    private Gender gender;
    private LocalDate registrationDate;
    private UUID addressId;
}
