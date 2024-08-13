package com.example.buyme.scheduler;

import com.example.buyme.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderService orderService;

    // D+1: 배송 중으로 변경
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateOrderStatusToShipped() {
        orderService.updateOrdersToShipped();
    }

    // D+2: 배송 완료로 변경
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateOrderStatusToDelivered() {
        orderService.updateOrdersToDelivered();
    }
}