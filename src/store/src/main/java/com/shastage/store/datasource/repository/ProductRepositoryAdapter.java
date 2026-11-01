package com.shastage.store.datasource.repository;

import com.shastage.store.datasource.entity.ProductEntity;
import com.shastage.store.datasource.mapper.ProductEntityMapper;
import com.shastage.store.datasource.repository.jpa.ProductJpaRepository;
import com.shastage.store.domain.model.Client;
import com.shastage.store.domain.model.Product;
import com.shastage.store.domain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    ProductJpaRepository productJpaRepository;
    ProductEntityMapper productEntityMapper;

    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id).map(productEntityMapper::toDomain);
    }

    public List<Product> findAll() {
        return productJpaRepository.findAll()
                .stream()
                .map(productEntityMapper::toDomain)
                .filter(product -> product.getAvailableStock() > 0)
                .toList();
    }

    public void save(Product product) {
        productJpaRepository.save(productEntityMapper.toEntity(product));
    }

    public void deleteById(UUID id) {
        productJpaRepository.deleteById(id);
    }
}
