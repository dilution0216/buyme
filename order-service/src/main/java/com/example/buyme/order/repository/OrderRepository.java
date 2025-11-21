package com.example.buyme.order.repository;

import com.example.buyme.order.entity.Order;
import com.example.buyme.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // N+1 문제 발생 (사용하지 않음)
    List<Order> findAllByUserId(Long userId);

    // N+1 문제 해결: Fetch Join 적용
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "WHERE o.userId = :userId")
    List<Order> findAllByUserIdWithItems(@Param("userId") Long userId);

    // 특정 상태의 주문 조회 (Fetch Join)
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "WHERE o.userId = :userId AND o.orderStatus = :status")
    List<Order> findByUserIdAndStatusWithItems(
        @Param("userId") Long userId,
        @Param("status") OrderStatus status);

    // 날짜 범위로 조회 (Fetch Join)
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByDateRangeWithItems(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // 스케줄러용 쿼리 (기존 유지)
    List<Order> findByOrderStatusAndOrderDateBefore(OrderStatus status, LocalDateTime date);
}
