package com.shastage.store.domain.repository;

import com.shastage.store.domain.model.Client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {

    Optional<Client> findById(UUID id);
    List<Client> findAll(Integer limit, Integer offset);
    List<Client> findByNameAndSurname(String name, String surname);

    void save(Client client);

    void deleteById(UUID id);


}
