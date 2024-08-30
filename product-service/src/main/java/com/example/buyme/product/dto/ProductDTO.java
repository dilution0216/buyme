package com.example.buyme.product.dto;

import lombok.Data;

@Data
public class ProductDTO {

    private Long productId;
    private String productName;
    private String productDescription;
    private int productPrice;
    private int productStock;
    // 예: "RESERVED", "NORMAL" 상태
    private String productType;
}