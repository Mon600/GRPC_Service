package com.shastage.store.domain.command;

import java.util.UUID;

public record SupplierCreateCommand(
        String name,
        UUID addressId,
        String phoneNumber) {
}
