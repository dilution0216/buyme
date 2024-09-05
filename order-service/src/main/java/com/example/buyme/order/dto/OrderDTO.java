package com.example.buyme.order.dto;
import com.example.buyme.order.dto.OrderItemDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDTO {

    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private int orderAmount;
    private String orderStatus;
    private List<OrderItemDTO> orderItems;
}
