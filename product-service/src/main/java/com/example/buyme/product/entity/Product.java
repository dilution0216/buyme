package com.example.buyme.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = true)
    private String productDescription;

    @Column(nullable = false)
    private int productPrice;

    @Column(nullable = false)
    private int productStock;

    @Column(nullable = false)
    private String productType; // 예: "RESERVED", "NORMAL"
}
