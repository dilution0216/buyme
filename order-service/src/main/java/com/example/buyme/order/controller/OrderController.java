package com.example.buyme.order.controller;

import com.example.buyme.order.entity.Order;
import com.example.buyme.order.entity.OrderItem;
import com.example.buyme.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order, @RequestBody List<OrderItem> orderItems) {
        Order createdOrder = orderService.createOrder(order, orderItems);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
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
}
