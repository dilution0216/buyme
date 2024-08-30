package com.example.buyme.product.repository;

import com.example.buyme.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long productId);
    List<Product> findByProductType(String productType);

}