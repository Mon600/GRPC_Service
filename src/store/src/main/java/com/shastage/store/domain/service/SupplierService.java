package com.shastage.store.domain.service;

import com.shastage.store.domain.command.AddressPutCommand;
import com.shastage.store.domain.command.SupplierCreateCommand;
import com.shastage.store.domain.model.Address;
import com.shastage.store.domain.model.Supplier;
import com.shastage.store.domain.repository.AddressRepository;
import com.shastage.store.domain.repository.SupplierRepository;
import com.shastage.store.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SupplierService {

    SupplierRepository supplierRepository;
    AddressRepository addressRepository;

    @Transactional
    public Supplier findById(UUID id) {
        return supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Supplier with id " + id + " not exists.")
        );
    }

    @Transactional
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    @Transactional
    public void deleteById(UUID id) {
        supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Supplier with id " + id + " not found!")
        );
        supplierRepository.deleteById(id);
    }

    @Transactional
    public Supplier create(SupplierCreateCommand command) {

        addressRepository.findById(command.addressId()).orElseThrow(
                () -> new ResourceNotFoundException("Supplier with id " + command.addressId() + " not found!")
        );

        Supplier supplier = new Supplier(
                UUID.randomUUID(),
                command.name(),
                command.addressId(),
                command.phoneNumber()
        );

        supplierRepository.save(supplier);

        return supplier;
    }

    @Transactional
    public void changeAddress(UUID id, AddressPutCommand command) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Supplier with id " + id + " not found!")
        );

        Address address = addressRepository.findById(supplier.getAddressId()).orElseThrow(
                () -> new ResourceNotFoundException("Address with id " + supplier.getAddressId() + " not found!")
        );

        Address newAddress = new Address(
                address.getId(),
                command.country(),
                command.city(),
                command.street()
        );

        addressRepository.save(newAddress);
    }
}
