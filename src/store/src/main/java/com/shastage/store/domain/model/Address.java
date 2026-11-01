package com.shastage.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class Address {
    private UUID id;
    private String country;
    private String city;
    private String street;
}
