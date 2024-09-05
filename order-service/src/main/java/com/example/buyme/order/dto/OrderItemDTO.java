package com.example.buyme.order.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
    private Long orderItemId;
    private Long productId;
    private int quantity;
}
