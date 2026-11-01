package com.shastage.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer availableStock;
    private Instant lastUpdateDate;
    private UUID supplierId;
    private UUID imageId;
}
