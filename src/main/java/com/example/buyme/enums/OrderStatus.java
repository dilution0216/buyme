package com.example.buyme.enums;

public enum OrderStatus {
    PENDING,        // 주문이 생성되었으나 아직 결제가 완료되지 않음
    PAID,           // 결제가 완료됨
    SHIPPED,        // 상품이 배송 중
    DELIVERED,      // 상품이 배송 완료됨
    CANCELED        // 주문이 취소됨
}