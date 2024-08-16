package com.example.buyme.order.entity;

import com.example.buyme.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int orderItemQuantity;

    @Column(nullable = false)
    private int orderItemPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus orderItemStatus;
}
