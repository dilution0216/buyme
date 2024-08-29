package com.example.buyme.order.dto;

import lombok.Data;

@Data
public class Product {
    private Long productId;
    private String productName;
    private String productDescription;
    private int productPrice;
    private int productStock;
}