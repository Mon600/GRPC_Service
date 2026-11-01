package com.shastage.store.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductEntity extends BaseEntity {

    private String name;

    private String category;

    private BigDecimal price;

    private Integer availableStock;

    private Instant lastUpdateDate;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "image_id")
    private UUID imageId;
}