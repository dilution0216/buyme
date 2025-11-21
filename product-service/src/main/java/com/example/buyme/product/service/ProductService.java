package com.example.buyme.product.service;

import com.example.buyme.product.dto.CreateProductDTO;
import com.example.buyme.product.dto.UpdateProductDTO;
import com.example.buyme.product.entity.Product;
import com.example.buyme.product.exception.InsufficientStockException;
import com.example.buyme.product.exception.ProductNotFoundException;
import com.example.buyme.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final RedissonClient redissonClient;

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

    /**
     * 재고 차감 (분산락 적용)
     * - 동시성 보장이 가장 중요한 연산
     * - 재고 부족 시 명확한 에러 반환
     */
    @CacheEvict(value = {"stock", "productDetail"}, key = "#productId")
    public void decreaseStock(Long productId, int quantity) {
        String lockKey = "stock:lock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도 (대기 5초, 유지 3초)
            boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);

            if (!acquired) {
                throw new IllegalStateException("재고 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }

            // 상품 조회
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다. ID: " + productId));

            // 재고 검증
            if (product.getProductStock() < quantity) {
                throw new InsufficientStockException(
                    String.format("재고 부족 - 상품: %s, 현재: %d개, 요청: %d개",
                        product.getProductName(), product.getProductStock(), quantity));
            }

            // 재고 차감
            int newStock = product.getProductStock() - quantity;
            product.setProductStock(newStock);
            productRepository.save(product);

            log.info("재고 차감 성공 - 상품ID: {}, 상품명: {}, 차감: {}, 남은재고: {}",
                productId, product.getProductName(), quantity, newStock);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("재고 차감 중 오류가 발생했습니다.", e);
        } finally {
            // 락 해제 (현재 스레드가 보유한 경우만)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 재고 증가 (반품, 취소 시)
     */
    @CacheEvict(value = {"stock", "productDetail"}, key = "#productId")
    public void increaseStock(Long productId, int quantity) {
        String lockKey = "stock:lock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도 (대기 5초, 유지 3초)
            boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);

            if (!acquired) {
                throw new IllegalStateException("재고 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }

            // 상품 조회
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다. ID: " + productId));

            // 재고 증가
            int newStock = product.getProductStock() + quantity;
            product.setProductStock(newStock);
            productRepository.save(product);

            log.info("재고 증가 성공 - 상품ID: {}, 상품명: {}, 증가: {}, 남은재고: {}",
                productId, product.getProductName(), quantity, newStock);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("재고 증가 중 오류가 발생했습니다.", e);
        } finally {
            // 락 해제 (현재 스레드가 보유한 경우만)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
