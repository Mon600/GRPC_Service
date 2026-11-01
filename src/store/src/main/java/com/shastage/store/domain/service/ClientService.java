package com.shastage.store.domain.service;

import com.shastage.store.domain.command.AddressPutCommand;
import com.shastage.store.domain.command.ClientCreateCommand;
import com.shastage.store.domain.model.Address;
import com.shastage.store.domain.model.Client;
import com.shastage.store.domain.repository.AddressRepository;
import com.shastage.store.domain.repository.ClientRepository;
import com.shastage.store.exception.BadRequestException;
import com.shastage.store.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ClientService {

    ClientRepository clientRepository;
    AddressRepository addressRepository;

    @Transactional
    public Client create(ClientCreateCommand clientCreateCommand) {

        if (!addressRepository.existsById(clientCreateCommand.addressId())) {
            throw new BadRequestException("Address with id " + clientCreateCommand.addressId() + " does not exists!");
        }

        Client client = new Client(
                UUID.randomUUID(),
                clientCreateCommand.name(),
                clientCreateCommand.surname(),
                clientCreateCommand.birthday(),
                clientCreateCommand.gender(),
                clientCreateCommand.registrationDate(),
                clientCreateCommand.addressId()
        );

        clientRepository.save(client);

        return client;
    }

    @Transactional
    public Client findById(UUID id) {
        return clientRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Client with id " + id + " not found!")
        );
    }

    @Transactional
    public List<Client> findByNameAndSurname(String name, String surname) {
        return clientRepository.findByNameAndSurname(name, surname);
    }

    // TODO: добавить удаление АДРЕСА если он больше нигде не нужен
    @Transactional
    public void deleteById(UUID id) {
        Client client = clientRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Client with id " + id + " not found!")
        );
        clientRepository.deleteById(id);
    }

    @Transactional
    public List<Client> findAll(Integer limit, Integer offset) {
        return clientRepository.findAll(limit, offset);
    }

    @Transactional
    public void changeAddress(UUID clientId, AddressPutCommand putAddress) {
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new ResourceNotFoundException("Client with id " + clientId + " not found!")
        );

        Address address = addressRepository.findById(client.getAddressId()).orElseThrow(
                () -> new ResourceNotFoundException("Address with id " + client.getAddressId() + " not found!")
        );

        Address newAddress = new Address(
                address.getId(),
                putAddress.country(),
                putAddress.city(),
                putAddress.street()
        );

        addressRepository.save(newAddress);
    }
}
