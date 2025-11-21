package com.example.buyme.product.controller;

import com.example.buyme.product.dto.CreateProductDTO;
import com.example.buyme.product.dto.UpdateProductDTO;
import com.example.buyme.product.entity.Product;
import com.example.buyme.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reserved")
    public ResponseEntity<List<Product>> getReservedProducts() {
        List<Product> products = productService.getReservedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/normal")
    public ResponseEntity<List<Product>> getNormalProducts() {
        List<Product> products = productService.getNormalProducts();
        return ResponseEntity.ok(products);
    }

    // 제품 생성 엔드포인트
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductDTO productDTO) {
        Product createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(createdProduct);
    }

    // 제품 재고를 업데이트하기 위한 엔드포인트
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody UpdateProductDTO productDTO) {
        productService.updateProductStock(id, productDTO.getProductStock());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<Integer> getProductStock(@PathVariable Long id) {
        int stock = productService.getProductStock(id);
        return ResponseEntity.ok(stock);
    }

    // 재고 차감 API (분산락 적용)
    @PostMapping("/{productId}/decrease-stock")
    public ResponseEntity<Void> decreaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        productService.decreaseStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

    // 재고 증가 API (반품, 취소 시)
    @PostMapping("/{productId}/increase-stock")
    public ResponseEntity<Void> increaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        productService.increaseStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

}
