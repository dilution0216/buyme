package com.example.buyme.order.repository;

import com.example.buyme.order.entity.Order;
import com.example.buyme.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Long userId);  // 사용자 ID로 모든 주문 조회

    List<Order> findByOrderStatusAndOrderDateBefore(OrderStatus status, LocalDateTime date);
}
