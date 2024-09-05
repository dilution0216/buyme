package com.example.buyme.order.controller;

import com.example.buyme.order.dto.OrderDTO;
import com.example.buyme.order.dto.OrderRequest;
import com.example.buyme.order.entity.Order;
import com.example.buyme.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Order createdOrder = orderService.createOrder(orderRequest);
            OrderDTO orderDTO = convertToDTO(createdOrder);  // 엔티티를 DTO로 변환
            return ResponseEntity.ok(orderDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    private OrderDTO convertToDTO(Order order) {
        return orderService.convertToDTO(order);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{itemId}/return")
    public ResponseEntity<Void> requestReturn(@PathVariable Long itemId) {
        orderService.requestReturn(itemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{itemId}/complete-return")
    public ResponseEntity<Void> completeReturn(@PathVariable Long itemId) {
        orderService.completeReturn(itemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/enter-payment")
    public ResponseEntity<Void> enterPayment(@PathVariable Long orderId) {
        orderService.enterPayment(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/attempt-payment")
    public ResponseEntity<Void> attemptPayment(@PathVariable Long orderId) {
        boolean success = orderService.attemptPayment(orderId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        }
    }
}