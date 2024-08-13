package com.example.buyme.enums;

public enum OrderItemStatus {
    ORDERED,        // 상품이 주문됨
    SHIPPED,        // 상품이 배송 중
    DELIVERED,      // 상품이 배송 완료됨
    RETURN_REQUESTED, // 반품 요청됨
    RETURNED,       // 반품 완료됨
    CANCELED        // 상품이 취소됨
}