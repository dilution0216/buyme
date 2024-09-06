package com.example.buyme.order.service;

import com.example.buyme.order.dto.OrderDTO;
import com.example.buyme.order.dto.OrderItemDTO;
import com.example.buyme.order.dto.OrderRequest;
import com.example.buyme.order.entity.Order;
import com.example.buyme.order.entity.OrderItem;
import com.example.buyme.order.enums.OrderItemStatus;
import com.example.buyme.order.enums.OrderStatus;
import com.example.buyme.order.repository.OrderItemRepository;
import com.example.buyme.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RedissonClient redissonClient;


//    public Order createOrder(OrderRequest orderRequest) {
//        RLock lock = redissonClient.getLock("userOrderLock:" + orderRequest.getUserId());
//        try {
//            boolean isLocked = lock.tryLock(10, 2, TimeUnit.SECONDS);
//            if (!isLocked) {
//                throw new IllegalStateException("Another order is being processed for this user.");
//            }
//
//            Order order = new Order();
//            order.setUserId(orderRequest.getUserId());
//            order.setOrderDate(LocalDateTime.now());
//            order.setOrderStatus(OrderStatus.PENDING);
//
//            List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
//                    .map(itemRequest -> {
//                        OrderItem item = new OrderItem();
//                        item.setProductId(itemRequest.getProductId());
//                        item.setOrderItemQuantity(itemRequest.getQuantity());
//                        item.setOrderItemStatus(OrderItemStatus.ORDERED);
//                        item.setOrder(order);
//                        return item;
//                    }).collect(Collectors.toList());
//
//            order.setOrderItems(orderItems);
//            orderRepository.save(order);
//
//            return order;
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Error acquiring the lock", e);
//        } finally {
//            lock.unlock();
//        }
//    }



//     주문 생성 메서드
    public Order createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemRequest.getProductId());
                    item.setOrderItemQuantity(itemRequest.getQuantity());
                    item.setOrderItemStatus(OrderItemStatus.ORDERED);
                    item.setOrder(order);  // OrderItem에 Order 설정
                    return item;
                }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        orderRepository.save(order);

        return order;
    }

    // 사용자별 주문 목록 조회
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findAllByUserId(userId);
    }

    // DTO 변환 메서드
    public OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setOrderAmount(order.getOrderAmount());
        orderDTO.setOrderStatus(order.getOrderStatus().name());
        orderDTO.setOrderItems(order.getOrderItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        return orderDTO;
    }

    private OrderItemDTO convertToDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderItemId(orderItem.getOrderItemId());
        orderItemDTO.setProductId(orderItem.getProductId());
        orderItemDTO.setQuantity(orderItem.getOrderItemQuantity());
        return orderItemDTO;
    }

    // 주문 취소 메서드
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 주문 ID입니다"));

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송되었거나 배송 완료된 주문은 취소할 수 없습니다");
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    // 반품 요청 메서드
    public void requestReturn(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 주문 항목 ID입니다"));

        if (orderItem.getOrderItemStatus() != OrderItemStatus.DELIVERED) {
            throw new IllegalStateException("배송 완료된 항목만 반품할 수 있습니다");
        }

        orderItem.setOrderItemStatus(OrderItemStatus.RETURN_REQUESTED);
        orderItemRepository.save(orderItem);
    }

    // 반품 완료 처리 메서드
    public void completeReturn(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 주문 항목 ID입니다"));

        orderItem.setOrderItemStatus(OrderItemStatus.RETURNED);
        orderItemRepository.save(orderItem);
    }

    // 결제 처리 시작 메서드
    public void enterPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 주문 ID입니다"));

        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("결제 가능한 상태가 아닙니다");
        }

        order.setOrderStatus(OrderStatus.PAYMENT_ENTERED);
        orderRepository.save(order);
    }

//    public boolean attemptPayment(Long orderId) {
//        RLock lock = redissonClient.getLock("orderLock:" + orderId);
//        try {
//            // 락 획득 시도 (10초 동안 락을 획득하고, 2초간 유지)
//            boolean isLocked = lock.tryLock(3, 1, TimeUnit.SECONDS);
//            if (!isLocked) {
//                throw new IllegalStateException("Order is being processed by another request.");
//            }
//
//            // 주문 정보 조회 및 결제 시도 로직
//            Order order = orderRepository.findById(orderId)
//                    .orElseThrow(() -> new IllegalArgumentException("잘못된 주문 ID입니다"));
//
//            if (Math.random() > 0.2) {  // 80% 확률로 결제 성공
//                order.setOrderStatus(OrderStatus.PAID);
//                orderRepository.save(order);
//                return true;
//            } else {
//                order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
//                orderRepository.save(order);
//                return false;
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Error acquiring the lock", e);
//        } finally {
//            lock.unlock();  // 락 해제
//        }
//    }

//    // 결제 시도 메서드
    public boolean attemptPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 주문 ID입니다"));

        // 결제 시도 로직
        if (Math.random() > 0.2) {  // 80% 확률로 결제 성공
            order.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(order);
            return true;
        } else {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            return false;
        }
    }

    // 배송 중으로 상태 변경 (D+1)
    public void updateOrdersToShipped() {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        List<Order> orders = orderRepository.findByOrderStatusAndOrderDateBefore(OrderStatus.PAID, now);

        for (Order order : orders) {
            order.setOrderStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        }
    }

    // 배송 완료로 상태 변경 (D+2)
    public void updateOrdersToDelivered() {
        LocalDateTime now = LocalDateTime.now().minusDays(2);
        List<Order> orders = orderRepository.findByOrderStatusAndOrderDateBefore(OrderStatus.SHIPPED, now);

        for (Order order : orders) {
            order.setOrderStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }
    }
}
