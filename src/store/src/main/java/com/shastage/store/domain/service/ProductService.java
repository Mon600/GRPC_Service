package com.shastage.store.domain.service;

import com.shastage.store.domain.command.ProductCreateCommand;
import com.shastage.store.domain.model.Product;
import com.shastage.store.domain.repository.ProductRepository;
import com.shastage.store.domain.repository.SupplierRepository;
import com.shastage.store.domain.repository.ImageRepository;
import com.shastage.store.exception.BadRequestException;
import com.shastage.store.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService {

    ProductRepository productRepository;
    SupplierRepository supplierRepository;
    ImageRepository imageRepository;

    @Transactional
    public Product findById(UUID id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + id + " not found!")
        );
    }

    @Transactional
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional
    public void decreaseStock(UUID id, Integer decreaseBy) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + id + " not found!")
        );

        if (product.getAvailableStock() - decreaseBy < 0) {
            throw new BadRequestException("There are not so many products.");
        }

        product.setAvailableStock(product.getAvailableStock() - decreaseBy);

        productRepository.save(product);
    }

    @Transactional
    public Product create(ProductCreateCommand command) {
        supplierRepository.findById(command.supplierId()).orElseThrow(
                () -> new BadRequestException("Supplier with id " + command.supplierId() + " not found!")
        );
        imageRepository.findById(command.imageId()).orElseThrow(
                () -> new BadRequestException("Image with id " + command.imageId() + " not found!")
        );

        Product product = new Product(
                UUID.randomUUID(),
                command.name(),
                command.category(),
                command.price(),
                command.availableStock(),
                Instant.now(),
                command.supplierId(),
                command.imageId()
        );

        productRepository.save(product);

        return product;
    }

    @Transactional
    public void deleteById(UUID id) {
        productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + id + " not found!")
        );
        productRepository.deleteById(id);
    }
}
