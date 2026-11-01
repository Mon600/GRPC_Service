package com.shastage.store.datasource.entity;

import com.shastage.store.domain.model.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "clients")
@AllArgsConstructor
@NoArgsConstructor
public class ClientEntity extends BaseEntity {
    private String name;

    private String surname;

    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate registrationDate;

    @Column(name = "address_id")
    private UUID addressId;
}
