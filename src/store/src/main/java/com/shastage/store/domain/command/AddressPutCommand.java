package com.shastage.store.domain.command;

public record AddressPutCommand(
        String country,
        String city,
        String street
) {
}
