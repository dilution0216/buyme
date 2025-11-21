package com.example.buyme.product.service;

import com.example.buyme.product.dto.CreateProductDTO;
import com.example.buyme.product.dto.UpdateProductDTO;
import com.example.buyme.product.entity.Product;
import com.example.buyme.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 생성 (모든 상품 목록 캐시 무효화)
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(CreateProductDTO productDTO) {
        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setProductDescription(productDTO.getProductDescription());
        product.setProductPrice(productDTO.getProductPrice());
        product.setProductStock(productDTO.getProductStock());
        product.setProductType(productDTO.getProductType());
        return productRepository.save(product);
    }

    // 모든 상품 조회 (캐싱)
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 상품 상세 조회 (캐싱)
    @Cacheable(value = "productDetail", key = "#productId")
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    // 예약 상품 목록 조회 (캐싱)
    @Cacheable(value = "products", key = "'RESERVED'")
    public List<Product> getReservedProducts() {
        return productRepository.findByProductType("RESERVED");
    }

    // 일반 상품 목록 조회 (캐싱)
    @Cacheable(value = "products", key = "'NORMAL'")
    public List<Product> getNormalProducts() {
        return productRepository.findByProductType("NORMAL");
    }

    // 재고 조회 (캐싱)
    @Cacheable(value = "stock", key = "#productId")
    public int getProductStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return product.getProductStock();
    }

    // 제품 재고 업데이트 (캐시 무효화)
    @CacheEvict(value = {"stock", "productDetail"}, key = "#productId")
    public void updateProductStock(Long productId, int newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setProductStock(newStock);
        productRepository.save(product);
    }
}
