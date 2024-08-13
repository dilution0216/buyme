package com.example.buyme.service;

import com.example.buyme.entity.Order;
import com.example.buyme.entity.OrderItem;
import com.example.buyme.entity.Product;
import com.example.buyme.enums.OrderItemStatus;
import com.example.buyme.enums.OrderStatus;
import com.example.buyme.repository.OrderItemRepository;
import com.example.buyme.repository.OrderRepository;
import com.example.buyme.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public Order createOrder(Order order, List<OrderItem> orderItems) {
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderItems(orderItems);
        orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            item.setOrderItemStatus(OrderItemStatus.ORDERED);
            orderItemRepository.save(item);
        }

        return order;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findAllByUserUserId(userId);
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
            Product product = item.getProduct();
            product.setProductStock(product.getProductStock() + item.getOrderItemQuantity());
            productRepository.save(product);
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
        Product product = orderItem.getProduct();
        product.setProductStock(product.getProductStock() + orderItem.getOrderItemQuantity());
        productRepository.save(product);
    }
}