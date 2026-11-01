package com.shastage.store.datasource.repository.jpa;

import com.shastage.store.datasource.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
    List<ClientEntity> findClientEntityByNameAndSurname(String name, String surname);

    @Query(value = """
    SELECT *
    FROM clients
    ORDER BY id
    LIMIT :limit
    OFFSET :offset;
    """, nativeQuery = true)
    List<ClientEntity> findAll(@Param("limit") Integer limit,
                               @Param("offset") Integer offset);
}
