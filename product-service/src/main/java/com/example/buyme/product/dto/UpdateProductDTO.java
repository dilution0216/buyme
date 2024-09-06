package com.example.buyme.product.dto;

import lombok.Data;

@Data
public class UpdateProductDTO {
    private Long productId;
    private int productStock;
}
