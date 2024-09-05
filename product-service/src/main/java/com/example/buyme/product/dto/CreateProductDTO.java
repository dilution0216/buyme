package com.example.buyme.product.dto;

import lombok.Data;

@Data
public class CreateProductDTO {
    private String productName;
    private String productDescription;
    private int productPrice;
    private int productStock;
    private String productType;
}
