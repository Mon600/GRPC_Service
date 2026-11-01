package com.shastage.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Supplier {
    private UUID id;
    private String name;
    private UUID addressId;
    private String phoneNumber;
}
