package com.example.buyme.product.service;

import com.example.buyme.product.entity.Product;
import com.example.buyme.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    public List<Product> getReservedProducts() {
        return productRepository.findByProductType("RESERVED");
    }

    public List<Product> getNormalProducts() {
        return productRepository.findByProductType("NORMAL");
    }

}