package com.shastage.store.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "suppliers")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierEntity extends BaseEntity {

    private String name;

    @Column(name = "address_id")
    private UUID addressId;

    private String phoneNumber;
}
