package com.example.buyme.order.enums;

public enum OrderStatus {
    PENDING,        // 주문이 생성되었으나 아직 결제가 완료되지 않음
    PAID,           // 결제가 완료됨
    SHIPPED,        // 상품이 배송 중
    DELIVERED,      // 상품이 배송 완료됨
    CANCELED,        // 주문이 취소됨
    PAYMENT_ENTERED,  // 결제 화면 진입
    PAYMENT_FAILED    // 결제 실패

    }