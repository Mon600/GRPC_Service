package com.shastage.store.datasource.repository;

import com.shastage.store.datasource.mapper.ImageEntityMapper;
import com.shastage.store.datasource.repository.jpa.ImageJpaRepository;
import com.shastage.store.domain.model.Image;
import com.shastage.store.domain.repository.ImageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class ImageRepositoryAdapter implements ImageRepository {

    ImageJpaRepository imageJpaRepository;
    ImageEntityMapper imageEntityMapper;

    public Optional<Image> findById(UUID id) {
        return imageJpaRepository.findById(id).map(imageEntityMapper::toDomain);
    }

    public void save(Image image) {
        imageJpaRepository.save(imageEntityMapper.toEntity(image));
    }

    public void deleteById(UUID id) {
        imageJpaRepository.deleteById(id);
    }
}
