package com.shastage.store.domain.repository;

import com.shastage.store.domain.model.Image;

import java.util.Optional;
import java.util.UUID;

public interface ImageRepository {
    Optional<Image> findById(UUID id);
    void save(Image image);
    void deleteById(UUID id);
}
