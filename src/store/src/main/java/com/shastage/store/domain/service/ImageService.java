package com.shastage.store.domain.service;

import com.shastage.store.domain.command.ImageCreateCommand;
import com.shastage.store.domain.model.Image;
import com.shastage.store.domain.model.Product;
import com.shastage.store.domain.repository.ImageRepository;
import com.shastage.store.domain.repository.ProductRepository;
import com.shastage.store.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ImageService {

    ImageRepository imageRepository;
    ProductRepository productRepository;

    @Transactional
    public Image create(ImageCreateCommand command) {
        Product product = productRepository.findById(command.productId()).orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + command.productId() + " not found!")
        );

        Image image = new Image(UUID.randomUUID(), command.image());
        imageRepository.save(image);

        product.setImageId(image.getId());
        productRepository.save(product);

        return image;
    }

    @Transactional
    public void update(UUID id, byte[] newImage) {
        Image image = imageRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Image with id " + id + " not found!")
        );

        image.setImage(newImage);
        imageRepository.save(image);
    }

    @Transactional
    public void deleteById(UUID id) {
        imageRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Image with id " + id + " not found!")
        );
        imageRepository.deleteById(id);
    }

    @Transactional
    public Image findByProductId(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + productId + " not found!")
        );

        if (product.getImageId() == null) {
            throw new ResourceNotFoundException("Product with id " + productId + " has no image!");
        }

        return imageRepository.findById(product.getImageId()).orElseThrow(
                () -> new ResourceNotFoundException("Image with id " + product.getImageId() + " not found!")
        );
    }

    @Transactional
    public Image findById(UUID id) {
        return imageRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Image with id " + id + " not found!")
        );
    }
}
