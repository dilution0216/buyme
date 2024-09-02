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
    // 제품 재고를 업데이트하는 로직
    public void updateProductStock(Long productId, int newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setProductStock(newStock);
        productRepository.save(product);
    }


}