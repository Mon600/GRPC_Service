package com.shastage.store.domain.command;

import java.util.UUID;

public record ImageCreateCommand(
        byte[] image,
        UUID productId
) {
}
