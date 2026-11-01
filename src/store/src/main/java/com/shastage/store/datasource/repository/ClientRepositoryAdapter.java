package com.shastage.store.datasource.repository;

import com.shastage.store.datasource.entity.ClientEntity;
import com.shastage.store.datasource.mapper.ClientEntityMapper;
import com.shastage.store.datasource.repository.jpa.ClientJpaRepository;
import com.shastage.store.domain.model.Client;
import com.shastage.store.domain.repository.ClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Repository
public class ClientRepositoryAdapter implements ClientRepository {

    ClientEntityMapper clientEntityMapper;
    ClientJpaRepository clientJpaRepository;

    @Override
    public void save(Client client) {
        ClientEntity clientEntity = clientEntityMapper.toEntity(client);
        clientJpaRepository.save(clientEntity);
    }

    @Override
    public List<Client> findByNameAndSurname(String name, String surname) {
        List<ClientEntity> entities = clientJpaRepository.findClientEntityByNameAndSurname(name, surname);
        return entities.stream()
                .map(clientEntityMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        clientJpaRepository.deleteById(id);
    }

    @Override
    public Optional<Client> findById(UUID id) {
        Optional<ClientEntity> entity = clientJpaRepository.findById(id);
        return entity.map(clientEntityMapper::toDomain);
    }

    @Override
    public List<Client> findAll(Integer limit, Integer offset) {
        List<ClientEntity> entities = clientJpaRepository.findAll(limit, offset);
        return entities.stream()
                .map(clientEntityMapper::toDomain)
                .toList();
    }
}
