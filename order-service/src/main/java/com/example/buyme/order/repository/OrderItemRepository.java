package com.example.buyme.order.repository;

import com.example.buyme.order.entity.OrderItem;
import com.example.buyme.order.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderItemStatusAndOrderOrderDateBefore(OrderItemStatus status, LocalDateTime date);
}