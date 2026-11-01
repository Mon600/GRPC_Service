package app.datalayer.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "email", unique = true, length = 255)
    private String email;

    @NotNull
    @Column(name = "name", length = 255)
    private String name;

    @NotNull
    @Column(name = "surname", length = 255)
    private String surname;

    @NotNull
    @Column(name = "phone", unique = true, length = 15)
    private String phone;

    @NotNull
    @Column(name = "password_hash", length = 255)
    private String password;
}
