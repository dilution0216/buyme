package com.example.buyme.order.service;

import com.example.buyme.order.dto.Product;
import com.example.buyme.order.entity.Order;
import com.example.buyme.order.entity.OrderItem;
import com.example.buyme.order.enums.OrderItemStatus;
import com.example.buyme.order.enums.OrderStatus;
import com.example.buyme.order.repository.OrderItemRepository;
import com.example.buyme.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebClient.Builder webClientBuilder;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STOCK_LOCK_PREFIX = "product_stock_lock_";

    // Product 정보를 API를 통해 가져오는 메소드
    private Product getProductById(Long productId) {
        return webClientBuilder.build()
                .get()
                .uri("http://gateway-service/products/" + productId)
                .retrieve()
                .bodyToMono(Product.class)
                .block();
    }

    // Product 재고를 업데이트하는 메소드
    private void updateProductStockWithLock(Long productId, int quantity) {
        String lockKey = STOCK_LOCK_PREFIX + productId;
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(5));

        if (Boolean.TRUE.equals(lock)) {
            try {
                Product product = getProductById(productId);
                if (product.getProductStock() < quantity) {
                    throw new RuntimeException("재고가 부족합니다.");
                }
                product.setProductStock(product.getProductStock() - quantity);

                // product-service에 업데이트 요청
                webClientBuilder.build()
                        .put()
                        .uri("http://gateway-service/api/products/" + productId)
                        .bodyValue(product)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            throw new RuntimeException("잠금 획득에 실패했습니다. 다시 시도해주세요.");
        }
    }

    public Order createOrder(Order order, List<OrderItem> orderItems) {
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderItems(orderItems);
        orderRepository.save(order);

        for (OrderItem item : orderItems) {
            updateProductStockWithLock(item.getProductId(), item.getOrderItemQuantity());
            item.setOrder(order);
            item.setOrderItemStatus(OrderItemStatus.ORDERED);
            orderItemRepository.save(item);
        }

        return order;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findAllByUserId(userId);
    }

    public void updateOrdersToShipped() {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        List<Order> orders = orderRepository.findByOrderStatusAndOrderDateBefore(OrderStatus.PAID, now);

        for (Order order : orders) {
            order.setOrderStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        }
    }

    public void updateOrdersToDelivered() {
        LocalDateTime now = LocalDateTime.now().minusDays(2);
        List<Order> orders = orderRepository.findByOrderStatusAndOrderDateBefore(OrderStatus.SHIPPED, now);

        for (Order order : orders) {
            order.setOrderStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("잘못된 주문 ID입니다"));

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송되었거나 배송 완료된 주문은 취소할 수 없습니다");
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        // 재고 복구
        for (OrderItem item : order.getOrderItems()) {
            updateProductStockWithLock(item.getProductId(), -item.getOrderItemQuantity());
        }
    }

    public void requestReturn(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(() -> new IllegalArgumentException("잘못된 주문 항목 ID입니다"));

        if (orderItem.getOrderItemStatus() != OrderItemStatus.DELIVERED) {
            throw new IllegalStateException("배송 완료된 항목만 반품할 수 있습니다");
        }

        LocalDateTime now = LocalDateTime.now();
        if (orderItem.getOrder().getOrderDate().plusDays(3).isBefore(now)) {
            throw new IllegalStateException("반품 기한이 만료되었습니다");
        }

        orderItem.setOrderItemStatus(OrderItemStatus.RETURN_REQUESTED);
        orderItemRepository.save(orderItem);
    }

    public void completeReturn(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(() -> new IllegalArgumentException("잘못된 주문 항목 ID입니다"));

        orderItem.setOrderItemStatus(OrderItemStatus.RETURNED);
        orderItemRepository.save(orderItem);

        // 재고 복구
        updateProductStockWithLock(orderItem.getProductId(), orderItem.getOrderItemQuantity());
    }

    public void enterPayment(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("잘못된 주문 ID입니다"));
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("결제 가능 상태가 아닙니다");
        }
        order.setOrderStatus(OrderStatus.PAYMENT_ENTERED);
        orderRepository.save(order);
    }

    public boolean attemptPayment(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("잘못된 주문 ID입니다"));
        if (Math.random() > 0.2) {
            order.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(order);
            return true;
        } else {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            return false;
        }
    }
}