package com.shastage.store.domain.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreateCommand(
        String name,
        String category,
        BigDecimal price,
        Integer availableStock,
        UUID supplierId,
        UUID imageId) {
}
