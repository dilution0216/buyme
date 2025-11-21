# BuyMe 프로젝트 종합 분석 보고서

**분석 일자**: 2025-11-21
**프로젝트**: BuyMe - 선착순 구매 서비스
**아키텍처**: Microservices Architecture (MSA)

---

## 목차

1. [Executive Summary](#1-executive-summary)
2. [프로젝트 개요](#2-프로젝트-개요)
3. [아키텍처 분석](#3-아키텍처-분석)
4. [서비스별 상세 분석](#4-서비스별-상세-분석)
5. [동시성 제어 메커니즘 분석](#5-동시성-제어-메커니즘-분석)
6. [데이터베이스 및 영속성 분석](#6-데이터베이스-및-영속성-분석)
7. [Redis 활용 전략](#7-redis-활용-전략)
8. [보안 분석](#8-보안-분석)
9. [코드 품질 평가](#9-코드-품질-평가)
10. [성능 및 확장성](#10-성능-및-확장성)
11. [개선 권장사항](#11-개선-권장사항)
12. [결론](#12-결론)

---

## 1. Executive Summary

### 프로젝트 현황
BuyMe는 선착순 구매 서비스를 제공하는 MSA 기반 프로젝트로, 모노리스 아키텍처에서 마이크로서비스로 성공적으로 전환되었습니다. Docker Compose 환경에서 4개의 독립적인 서비스가 운영되며, 대규모 동시 트래픽(약 10,000건)을 처리할 수 있도록 설계되었습니다.

### 주요 성과
- ✅ MSA로의 성공적인 전환
- ✅ Redis 기반 동시성 제어 구현
- ✅ 낙관적 락 도입으로 성능 15-40% 향상
- ✅ JWT 기반 인증/인가 시스템
- ✅ 자동화된 배송 상태 관리

### 핵심 발견사항
1. **동시성 제어 최적화**: 분산락에서 낙관적 락으로 전환하여 성능 개선
2. **간결한 아키텍처**: 불필요한 복잡성 없이 명확한 서비스 분리
3. **개선 여지**: 보안, 에러 처리, 모니터링 영역에서 강화 필요

---

## 2. 프로젝트 개요

### 2.1 비즈니스 목표
- 수량 제한 상품의 선착순 판매 시스템
- 대규모 동시 접속 처리 (10,000+ requests)
- 재고 관리 및 주문 처리 자동화

### 2.2 기술 스택

| 카테고리 | 기술 |
|---------|------|
| **언어** | Java 21 |
| **프레임워크** | Spring Boot 3.3.2 |
| **데이터베이스** | MySQL 8.0 |
| **캐시/락** | Redis (Redisson 3.16.3) |
| **보안** | Spring Security, JWT (jjwt 0.11.5) |
| **컨테이너** | Docker, Docker Compose |
| **빌드 도구** | Gradle |
| **테스팅** | JMeter, JUnit |

### 2.3 서비스 구성

```
                     ┌──────────────────┐
                     │   API Gateway    │
                     │   (Port 8080)    │
                     └────────┬─────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
    ┌────▼────┐         ┌────▼────┐         ┌────▼────┐
    │  User   │         │ Product │         │  Order  │
    │ Service │         │ Service │         │ Service │
    │  :8081  │         │  :8082  │         │  :8083  │
    └────┬────┘         └────┬────┘         └────┬────┘
         │                    │                    │
         └────────────────────┼────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
              ┌─────▼─────┐       ┌────▼────┐
              │   MySQL   │       │  Redis  │
              │   :3306   │       │  :6379  │
              └───────────┘       └─────────┘
```

---

## 3. 아키텍처 분석

### 3.1 MSA 전환 평가

#### 장점
1. **서비스 독립성**: 각 서비스가 독립적으로 배포 가능
2. **기술 스택 유연성**: 서비스별 최적 기술 선택 가능
3. **확장성**: 부하에 따라 특정 서비스만 스케일 아웃 가능
4. **장애 격리**: 한 서비스의 장애가 다른 서비스에 미치는 영향 최소화

#### 현재 구현의 특징
- **통신 방식**: 동기식 HTTP 통신 (WebFlux 사용)
- **데이터 공유**: 단일 MySQL 데이터베이스 공유 (서비스별 테이블 분리)
- **서비스 디스커버리**: 없음 (정적 URL 사용)
- **API Gateway**: Spring Cloud Gateway 활용

#### 개선 필요 영역
1. **서비스 간 결합도**: 데이터베이스 공유로 인한 결합 (DB per Service 패턴 미적용)
2. **서비스 디스커버리**: Eureka 등 동적 서비스 발견 메커니즘 부재
3. **이벤트 기반 통신**: 비동기 메시징 시스템 미도입
4. **분산 추적**: 로그 상관관계 ID, 분산 트레이싱 미구현

### 3.2 컨테이너 오케스트레이션

**Docker Compose 분석** (`docker-compose.yml`)

#### 서비스 구성
```yaml
services:
  - db (MySQL 8.0)
  - redis (latest)
  - user (Port 8081)
  - product (Port 8082)
  - order (Port 8083)
  - gateway (Port 8080)
  - adminer (DB 관리 도구, Port 8084)
```

#### 특징
- ✅ 헬스체크 구현 (MySQL)
- ✅ 볼륨 영속성 (db_data, redis_data)
- ✅ 환경 변수를 통한 설정 주입
- ✅ depends_on을 통한 시작 순서 제어

#### 개선 필요사항
- ❌ 리소스 제한 (CPU, 메모리) 미설정
- ❌ 로그 드라이버 설정 부재
- ❌ 프로덕션 환경 분리 필요 (.env 파일 활용)
- ❌ Redis 헬스체크 미구현

---

## 4. 서비스별 상세 분석

### 4.1 Gateway Service

**역할**: API 라우팅 및 진입점

**주요 설정** (`application.properties`):
```properties
# User Service 라우팅
/api/auth/** → http://user:8081

# Product Service 라우팅
/api/products/** → http://product:8082

# Order Service 라우팅
/api/orders/** → http://order:8083
```

**평가**:
- ✅ **단순성**: 복잡도 없이 명확한 라우팅
- ✅ **Reactive 스택**: WebFlux 기반으로 비동기 처리
- ❌ **필터 부재**: CORS, 인증, 로깅 필터 미구현
- ❌ **Circuit Breaker**: Resilience4j 미적용
- ❌ **Rate Limiting**: 부하 제한 기능 없음

**권장사항**:
1. JWT 검증 필터 추가
2. CORS 설정
3. Rate Limiting 구현
4. Circuit Breaker 패턴 적용
5. 로깅 및 모니터링 강화

---

### 4.2 User Service

**역할**: 사용자 인증, 인가, 회원 관리

#### 4.2.1 도메인 모델

**User Entity** (`user-service/src/main/java/com/example/buyme/user/entity/User.java`):
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    private Long userId;

    private String userName;
    private String userEmail;        // unique
    private String userPassword;     // 암호화됨
    private String userPhoneNumber;
    private String address;
    private boolean emailVerified;   // 이메일 인증 여부
}
```

#### 4.2.2 인증/인가 메커니즘

**JWT 토큰 제공자** (`JwtTokenProvider.java`):
- **알고리즘**: HS512
- **만료 시간**: 86,400,000ms (24시간)
- **Secret Key**: Base64 인코딩된 고정 키 (⚠️ 보안 이슈)

**인증 흐름**:
1. 사용자 로그인 요청 (`LoginRequest`)
2. 이메일/비밀번호 검증 (BCrypt)
3. 이메일 인증 여부 확인
4. JWT 토큰 생성 및 반환

**보안 설정** (`SecurityConfig.java`):
- Spring Security 활용
- PasswordEncoder (BCrypt)
- 암호화 유틸리티 (`EncryptionUtil.java`)

#### 4.2.3 이메일 서비스

**기능**:
- 회원가입 시 인증 이메일 발송
- Gmail SMTP 사용

**문제점**:
- ⚠️ 이메일 비밀번호가 평문으로 `application.properties`에 노출

#### 4.2.4 예외 처리

**Custom Exceptions**:
- `DuplicateEmailException`
- `InvalidEmailFormatException`
- `EmailNotVerifiedException`
- `InvalidPhoneNumberException`

**GlobalExceptionHandler**:
- `@RestControllerAdvice` 기반
- 표준화된 에러 응답 제공

#### 4.2.5 평가

| 항목 | 상태 | 비고 |
|------|------|------|
| JWT 구현 | ✅ | 표준 라이브러리 사용 |
| 비밀번호 암호화 | ✅ | BCrypt 적용 |
| 이메일 인증 | ✅ | 2단계 인증 |
| 예외 처리 | ✅ | 체계적 구조 |
| Secret Key 관리 | ❌ | 환경 변수로 이동 필요 |
| 토큰 무효화 | ❌ | 로그아웃 시 미구현 (주석 처리) |
| Refresh Token | ❌ | 미구현 |

---

### 4.3 Product Service

**역할**: 상품 관리, 재고 관리, 위시리스트

#### 4.3.1 도메인 모델

**Product Entity**:
```java
@Entity
public class Product {
    @Id @GeneratedValue
    private Long productId;

    private String productName;
    private String productDescription;
    private int productPrice;
    private int productStock;          // 재고
    private String productType;        // "RESERVED" | "NORMAL"
}
```

**WishList Entity**:
- 사용자별 위시리스트 관리

#### 4.3.2 비즈니스 로직

**ProductService**:
- 상품 CRUD
- 재고 유형별 조회 (RESERVED, NORMAL)
- 재고 업데이트

**문제점**:
- ⚠️ `updateProductStock()` 메서드에 동시성 제어 없음
- ⚠️ 재고 차감 시 경합 조건 가능

#### 4.3.3 Redis 설정

**RedisConfig.java**:
```java
@Bean
public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory());
    return template;
}
```

- ✅ Lettuce Connection Factory 사용
- ❌ 실제 캐싱 어노테이션 (@Cacheable 등) 미사용
- ❌ 캐시 전략 미구현

#### 4.3.4 평가

| 항목 | 상태 | 비고 |
|------|------|------|
| 상품 CRUD | ✅ | 기본 기능 구현 |
| 재고 관리 | ⚠️ | 동시성 제어 부재 |
| Redis 캐싱 | ❌ | 설정만 있고 미활용 |
| 위시리스트 | ✅ | 기능 제공 |

---

### 4.4 Order Service (핵심)

**역할**: 주문 생성, 결제 처리, 배송 상태 관리, 반품 처리

#### 4.4.1 도메인 모델

**Order Entity**:
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue
    private Long orderId;

    private Long userId;
    private LocalDateTime orderDate;
    private int orderAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @Version  // ⭐ 낙관적 락
    private int version;
}
```

**OrderItem Entity**:
```java
@Entity
public class OrderItem {
    @Id @GeneratedValue
    private Long orderItemId;

    @ManyToOne
    private Order order;

    private Long productId;
    private int orderItemQuantity;
    private int orderItemPrice;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;
}
```

#### 4.4.2 주문 상태 머신

**OrderStatus Enum**:
```
PENDING → PAYMENT_ENTERED → PAID → SHIPPED → DELIVERED
                    ↓
              PAYMENT_FAILED
                    ↓
                CANCELED
```

**OrderItemStatus Enum**:
```
ORDERED → SHIPPED → DELIVERED → RETURN_REQUESTED → RETURNED
   ↓
CANCELED
```

#### 4.4.3 핵심 비즈니스 로직

**1. 주문 생성** (`createOrder()`):
```java
public Order createOrder(OrderRequest orderRequest) {
    Order order = new Order();
    order.setUserId(orderRequest.getUserId());
    order.setOrderDate(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.PENDING);

    List<OrderItem> orderItems = ...;  // OrderItem 생성
    order.setOrderItems(orderItems);

    return orderRepository.save(order);
}
```

- **현재 상태**: 기본 메서드 활성화 (동시성 제어 없음)
- **주석 처리**: 분산락 버전 존재 (31-64행)

**2. 결제 처리** (`attemptPayment()`):
```java
public boolean attemptPayment(Long orderId) {
    try {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(...);

        // 80% 확률로 결제 성공 시뮬레이션
        if (Math.random() > 0.2) {
            order.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(order);
            return true;
        } else {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            return false;
        }
    } catch (OptimisticLockException e) {
        throw new RuntimeException("다른 요청이 먼저 처리되었습니다. 다시 시도해 주세요.", e);
    }
}
```

- **현재 상태**: 낙관적 락 활용 (⭐ 핵심)
- **주석 처리**: 분산락 버전 존재 (167-195행)

**3. 배송 상태 자동 업데이트** (`OrderStatusScheduler`):
```java
@Scheduled(cron = "0 0 0 * * ?")  // 매일 자정
public void updateOrderStatusToShipped() {
    // D+1: PAID → SHIPPED
    orderService.updateOrdersToShipped();
}

@Scheduled(cron = "0 0 0 * * ?")
public void updateOrderStatusToDelivered() {
    // D+2: SHIPPED → DELIVERED
    orderService.updateOrdersToDelivered();
}
```

#### 4.4.4 동시성 제어 전략 변화

**분산락 → 낙관적 락 전환의 배경**:

1. **분산락 버전** (주석 처리됨):
   ```java
   RLock lock = redissonClient.getLock("orderLock:" + orderId);
   boolean isLocked = lock.tryLock(3, 1, TimeUnit.SECONDS);
   ```
   - **문제점**:
     - 대량 요청 시 락 대기 시간 증가
     - 병목 현상 발생
     - 처리량 감소

2. **낙관적 락 버전** (현재 활성화):
   ```java
   @Version
   private int version;  // Order 엔티티

   catch (OptimisticLockException e) {
       // 충돌 시 재시도 안내
   }
   ```
   - **장점**:
     - 충돌이 적은 환경에서 고성능
     - 락 대기 없음
     - 읽기 작업 성능 우수

**성능 개선 결과** (README.md 참조):
- 주문 생성: 응답 시간 15-30% 개선, 처리량 2-30% 증가
- 결제 처리: 응답 시간 3-40% 개선, 처리량 5-30% 증가

#### 4.4.5 Redis 및 Redisson 설정

**RedissonConfig.java**:
```java
@Bean
public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://redis:6379");
    return Redisson.create(config);
}
```

- ✅ Redisson 클라이언트 구성됨
- ⚠️ 현재 분산락 비활성화 (주석 처리)
- ✅ 필요 시 즉시 활성화 가능

#### 4.4.6 평가

| 항목 | 상태 | 비고 |
|------|------|------|
| 주문 생성 | ✅ | 기본 기능 완벽 |
| 결제 처리 | ✅ | 시뮬레이션 구현 |
| 동시성 제어 | ✅ | 낙관적 락 적용 |
| 상태 자동화 | ✅ | 스케줄러 활용 |
| 반품 처리 | ✅ | 완전 구현 |
| 분산락 대안 | ✅ | 코드 보존 |

**강점**:
- 명확한 상태 머신
- 유연한 동시성 전략 (두 가지 옵션 보유)
- 자동화된 배송 관리

**개선점**:
- 재시도 로직 부재
- 트랜잭션 보상 메커니즘 부재
- 외부 결제 게이트웨이 연동 미구현

---

## 5. 동시성 제어 메커니즘 분석

### 5.1 낙관적 락 vs 분산락 비교

#### 5.1.1 낙관적 락 (Optimistic Lock)

**구현 방식**:
```java
// Order 엔티티
@Version
private int version;

// OrderService
catch (OptimisticLockException e) {
    throw new RuntimeException("다른 요청이 먼저 처리되었습니다. 다시 시도해 주세요.", e);
}
```

**작동 원리**:
1. 엔티티 조회 시 버전 번호 읽기
2. 업데이트 시 버전 번호 비교
3. 버전 불일치 시 `OptimisticLockException` 발생
4. 애플리케이션에서 재시도 또는 에러 처리

**장점**:
- ✅ 락 대기 시간 없음
- ✅ 읽기 성능 우수
- ✅ 데드락 없음
- ✅ 충돌이 적은 환경에 최적

**단점**:
- ❌ 충돌 시 재시도 필요
- ❌ 충돌 빈도 높으면 성능 저하
- ❌ 사용자 경험 저하 가능 (에러 메시지)

**적합한 시나리오**:
- 읽기가 많고 쓰기가 적은 경우
- 동시 수정 확률이 낮은 경우
- ⭐ BuyMe의 결제 처리 (충돌 드문 단순 구조)

---

#### 5.1.2 분산락 (Distributed Lock - Redisson)

**구현 방식** (주석 처리됨):
```java
RLock lock = redissonClient.getLock("orderLock:" + orderId);
try {
    boolean isLocked = lock.tryLock(3, 1, TimeUnit.SECONDS);
    if (!isLocked) {
        throw new IllegalStateException("Order is being processed...");
    }

    // 비즈니스 로직

} finally {
    lock.unlock();
}
```

**작동 원리**:
1. Redis에 락 키 생성 시도
2. 락 획득 성공 시 비즈니스 로직 실행
3. 작업 완료 후 락 해제
4. 타임아웃 시간 내 락 획득 실패 시 예외

**장점**:
- ✅ 강력한 동시성 제어
- ✅ 충돌 방지 보장
- ✅ 분산 환경에 적합

**단점**:
- ❌ 락 대기 시간 발생
- ❌ 성능 오버헤드
- ❌ Redis 장애 시 영향
- ❌ 데드락 가능성

**적합한 시나리오**:
- 재고 차감 같은 중요 연산
- 동시 수정이 빈번한 경우
- 데이터 정합성이 최우선인 경우

---

### 5.2 BuyMe의 선택: 낙관적 락

**의사결정 근거** (README 트러블슈팅 참조):

**문제 상황**:
- 분산락 적용 시 응답 시간 증가
- 처리량 감소
- 락 대기 병목 현상

**해결 과정**:
1. 락 획득 대기 시간 조정 → 효과 미미
2. 락 유지 시간 조정 → 문제 지속
3. **낙관적 락 도입** → 성능 대폭 개선

**결과**:
- 주문 생성: 15-30% 성능 향상
- 결제 처리: 3-40% 성능 향상

**정당성**:
- 결제 과정에서 충돌이 자주 발생하지 않음
- 단순한 구조
- 조회 작업이 많음

---

### 5.3 권장 하이브리드 전략

**제안**:
1. **주문 생성**: 낙관적 락 유지 (충돌 적음)
2. **결제 처리**: 낙관적 락 유지 (현재 적합)
3. **재고 차감**: 분산락 적용 (⚠️ 현재 미구현)

**재고 차감에 분산락이 필요한 이유**:
```java
// ProductService.updateProductStock()
public void updateProductStock(Long productId, int newStock) {
    Product product = productRepository.findById(productId)
        .orElseThrow(...);
    product.setProductStock(newStock);  // ⚠️ 동시성 제어 없음
    productRepository.save(product);
}
```

**개선 코드 예시**:
```java
public void decreaseStock(Long productId, int quantity) {
    RLock lock = redissonClient.getLock("stock:" + productId);
    try {
        lock.lock(5, TimeUnit.SECONDS);

        Product product = productRepository.findById(productId)
            .orElseThrow(...);

        if (product.getProductStock() < quantity) {
            throw new InsufficientStockException();
        }

        product.setProductStock(product.getProductStock() - quantity);
        productRepository.save(product);

    } finally {
        lock.unlock();
    }
}
```

---

## 6. 데이터베이스 및 영속성 분석

### 6.1 ERD (추론)

```
┌─────────────────┐
│     users       │
├─────────────────┤
│ userId (PK)     │
│ userName        │
│ userEmail       │ (unique)
│ userPassword    │
│ userPhoneNumber │
│ address         │
│ emailVerified   │
└─────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐       ┌─────────────────┐
│     orders      │       │    product      │
├─────────────────┤       ├─────────────────┤
│ orderId (PK)    │       │ productId (PK)  │
│ userId (FK)     │       │ productName     │
│ orderDate       │       │ productDesc     │
│ orderAmount     │       │ productPrice    │
│ orderStatus     │       │ productStock    │
│ version         │       │ productType     │
└────────┬────────┘       └─────────────────┘
         │ 1:N                     │
         ▼                         │
┌─────────────────┐                │
│  order_items    │                │
├─────────────────┤                │
│ orderItemId(PK) │                │
│ orderId (FK)    │◄───────────────┘
│ productId (FK)  │
│ quantity        │
│ price           │
│ status          │
└─────────────────┘

┌─────────────────┐
│   wish_list     │
├─────────────────┤
│ wishListId (PK) │
│ userId (FK)     │
│ productId (FK)  │
└─────────────────┘
```

### 6.2 JPA 매핑 분석

#### 6.2.1 연관 관계

**Order ↔ OrderItem** (1:N):
```java
// Order
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
private List<OrderItem> orderItems;

// OrderItem
@ManyToOne
@JoinColumn(name = "order_id", nullable = false)
private Order order;
```

- ✅ 양방향 매핑
- ✅ Cascade.ALL로 자동 영속화
- ✅ orphanRemoval 미사용 (개선 가능)

#### 6.2.2 인덱스 전략

**명시적 인덱스**: 없음

**자동 인덱스** (JPA 기본):
- Primary Key 인덱스
- Foreign Key 인덱스
- Unique 제약조건 (userEmail)

**권장 추가 인덱스**:
```sql
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_status ON orders(order_status);
CREATE INDEX idx_order_date ON orders(order_date);
CREATE INDEX idx_product_type ON product(product_type);
```

#### 6.2.3 쿼리 최적화

**N+1 문제 가능성**:
```java
// OrderService.getOrdersByUser()
public List<Order> getOrdersByUser(Long userId) {
    return orderRepository.findAllByUserId(userId);
    // ⚠️ Order 조회 후 OrderItem 조회 시 N+1 발생 가능
}
```

**개선**:
```java
@Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.userId = :userId")
List<Order> findAllByUserIdWithItems(@Param("userId") Long userId);
```

### 6.3 트랜잭션 관리

**현황**:
- Spring의 기본 `@Transactional` 사용
- 격리 수준: 기본값 (READ_COMMITTED)

**개선 제안**:
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public boolean attemptPayment(Long orderId) {
    // ...
}
```

---

## 7. Redis 활용 전략

### 7.1 현재 사용 현황

#### 7.1.1 설정

**Product Service**:
```java
@Bean
public RedisTemplate<String, Object> redisTemplate() {
    // Lettuce 기반 연결
}
```

**Order Service**:
```java
@Bean
public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://redis:6379");
    return Redisson.create(config);
}
```

#### 7.1.2 실제 활용도

| 서비스 | 캐싱 | 분산락 | 세션 | 기타 |
|--------|------|--------|------|------|
| User | ❌ | ❌ | ❌ | - |
| Product | ❌ | ❌ | ❌ | - |
| Order | ❌ | ⚠️ (주석) | ❌ | - |

**결론**: Redis 인프라는 구축되었으나 실제 활용도는 낮음

### 7.2 개선 제안

#### 7.2.1 캐싱 전략

**1. 상품 목록 캐싱**:
```java
@Cacheable(value = "products", key = "#productType")
public List<Product> getProductsByType(String productType) {
    return productRepository.findByProductType(productType);
}

@CacheEvict(value = "products", allEntries = true)
public Product createProduct(CreateProductDTO dto) {
    // ...
}
```

**2. 사용자 정보 캐싱**:
```java
@Cacheable(value = "users", key = "#userId")
public User getUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(...);
}
```

**3. 재고 캐싱**:
```java
@Cacheable(value = "stock", key = "#productId")
public int getProductStock(Long productId) {
    // ...
}
```

#### 7.2.2 JWT 블랙리스트

**로그아웃 토큰 무효화**:
```java
public void invalidateToken(String token) {
    String key = "jwt:blacklist:" + token;
    redisTemplate.opsForValue().set(key, "revoked", 24, TimeUnit.HOURS);
}

public boolean isTokenValid(String token) {
    String key = "jwt:blacklist:" + token;
    return !redisTemplate.hasKey(key);
}
```

#### 7.2.3 세션 관리

**Spring Session Redis**:
```gradle
implementation 'org.springframework.session:spring-session-data-redis'
```

---

## 8. 보안 분석

### 8.1 인증/인가

#### 8.1.1 JWT 토큰 보안

**현황**:
- ✅ HS512 알고리즘 사용
- ✅ 토큰 만료 시간 설정 (24시간)
- ❌ Secret Key가 `application.properties`에 평문 노출
- ❌ Refresh Token 미구현
- ❌ 토큰 블랙리스트 미구현 (주석 처리)

**취약점**:
```properties
# user-service/application.properties
jwt.secret=secretsecretsecret...  # ⚠️ 평문 노출
```

**개선**:
```bash
# 환경 변수로 관리
export JWT_SECRET=$(openssl rand -base64 64)
```

```properties
jwt.secret=${JWT_SECRET}
```

#### 8.1.2 비밀번호 보안

**현황**:
- ✅ BCrypt 해싱
- ✅ Spring Security PasswordEncoder 사용

**강점**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 8.2 데이터 보안

#### 8.2.1 민감 정보 노출

**심각한 문제**:
```properties
# user-service/application.properties
spring.mail.username=hglad0216@gmail.com
spring.mail.password=ocst�rzdh�kcpl�uhqk  # ⚠️ 평문 노출
```

```properties
# 모든 서비스 application.properties
spring.datasource.password=12345678  # ⚠️ 약한 비밀번호
```

**개선**:
1. 환경 변수 사용
2. Spring Cloud Config 도입
3. Vault 등 시크릿 관리 도구 사용

#### 8.2.2 SQL Injection

**현황**:
- ✅ JPA/Hibernate 사용으로 기본 방어
- ✅ Parameterized Query 사용

**주의사항**:
- Native Query 사용 시 주의 필요

### 8.3 API 보안

#### 8.3.1 CORS

**현황**: 미구현

**권장**:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
    }
}
```

#### 8.3.2 Rate Limiting

**현황**: 미구현

**권장**: Bucket4j 또는 Gateway 레벨에서 구현

#### 8.3.3 HTTPS

**현황**: HTTP 사용 (개발 환경)

**프로덕션**: TLS/SSL 인증서 필수

### 8.4 보안 체크리스트

| 항목 | 상태 | 우선순위 |
|------|------|----------|
| JWT Secret Key 외부화 | ❌ | 🔴 High |
| DB 비밀번호 강화 | ❌ | 🔴 High |
| 이메일 비밀번호 외부화 | ❌ | 🔴 High |
| HTTPS 적용 | ❌ | 🔴 High |
| CORS 설정 | ❌ | 🟡 Medium |
| Rate Limiting | ❌ | 🟡 Medium |
| Refresh Token | ❌ | 🟡 Medium |
| JWT 블랙리스트 | ❌ | 🟡 Medium |
| API 입력 검증 | ⚠️ | 🟡 Medium |
| 로그 민감 정보 마스킹 | ❌ | 🟢 Low |

---

## 9. 코드 품질 평가

### 9.1 아키텍처 패턴

**레이어드 아키텍처**:
```
Controller → Service → Repository → Entity
```

- ✅ 명확한 레이어 분리
- ✅ 의존성 방향 올바름
- ✅ DTO 변환 구현

### 9.2 SOLID 원칙

#### 9.2.1 단일 책임 원칙 (SRP)
- ✅ 서비스별 단일 도메인
- ✅ AuthService / UserService 분리
- ⚠️ OrderService가 다소 비대 (주문, 결제, 배송 모두 처리)

#### 9.2.2 의존성 역전 원칙 (DIP)
- ✅ Repository 인터페이스 사용
- ✅ 생성자 주입 (`@RequiredArgsConstructor`)

### 9.3 코드 스타일

#### 9.3.1 Lombok 활용
- ✅ `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- ✅ `@RequiredArgsConstructor` (불변성)
- ⚠️ `@Data` 남용 (Entity에 사용 시 주의)

#### 9.3.2 예외 처리
- ✅ Custom Exception 정의
- ✅ GlobalExceptionHandler 구현
- ❌ 일부 서비스에만 적용 (Product, Order는 미흡)

#### 9.3.3 로깅
- ✅ SLF4J + Lombok `@Slf4j` 사용
- ⚠️ 로그 레벨 및 구조화 미흡

### 9.4 테스트

**현황**:
- JUnit 의존성 포함
- 실제 테스트 코드 확인 불가 (소스 미제공)

**권장**:
- 단위 테스트 (Service Layer)
- 통합 테스트 (Repository)
- API 테스트 (Controller)

### 9.5 코드 중복

**발견 사항**:
```gradle
// user-service/build.gradle
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
// ... (중복 선언)
```

**개선**: 루트 `build.gradle`의 `subprojects` 블록 활용 (이미 부분 적용됨)

### 9.6 주석 처리된 코드

**문제**:
- OrderService에 대량의 주석 코드 (분산락 버전)
- 버전 관리 시스템으로 충분하므로 제거 권장

**장점**:
- 실험적 비교 및 전환 용이성
- 향후 롤백 가능성

**권장**:
- 별도 브랜치로 관리
- 또는 주석에 명확한 설명 추가

---

## 10. 성능 및 확장성

### 10.1 성능 테스트 결과

**README.md 트러블슈팅 참조**:

#### 10.1.1 분산락 → 낙관적 락 전환

**주문 생성**:
- 응답 시간: 15-30% 개선
- 처리량: 2-30% 증가

**결제 처리**:
- 응답 시간: 3-40% 개선
- 처리량: 5-30% 증가

**상세 결과**: https://dilution0216.tistory.com/294

### 10.2 병목 지점 분석

#### 10.2.1 데이터베이스

**잠재적 병목**:
1. N+1 쿼리 문제 (Order ↔ OrderItem)
2. 인덱스 부족
3. 단일 MySQL 인스턴스 (수평 확장 불가)

**개선**:
- Read Replica 도입
- 쿼리 최적화 (Fetch Join)
- 인덱스 추가

#### 10.2.2 Redis

**현황**: 거의 미활용

**개선 후 예상 효과**:
- 상품 목록 캐싱: 조회 성능 5-10배 향상
- 재고 조회 캐싱: DB 부하 80% 감소

#### 10.2.3 Gateway

**현황**: 필터 없음

**잠재적 병목**:
- Rate Limiting 부재 시 DDoS 취약
- 모든 요청이 백엔드 서비스 직격

### 10.3 확장성 전략

#### 10.3.1 수평 확장 (Scale-Out)

**현재 가능**:
- User Service 다중 인스턴스
- Product Service 다중 인스턴스
- Order Service 다중 인스턴스

**제약사항**:
- 단일 MySQL (Write 병목)
- 단일 Redis (고가용성 부족)

**개선**:
```yaml
# docker-compose.yml (예시)
order:
  deploy:
    replicas: 3
    resources:
      limits:
        cpus: '0.5'
        memory: 512M
```

#### 10.3.2 수직 확장 (Scale-Up)

**리소스 제한 설정 필요**:
```yaml
services:
  db:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 4G
```

### 10.4 캐싱 전략

**제안**:
1. **L1 Cache**: 애플리케이션 메모리 (Caffeine)
2. **L2 Cache**: Redis
3. **TTL 전략**:
   - 상품 목록: 5분
   - 상품 상세: 10분
   - 재고: 10초 (높은 변동성)

### 10.5 비동기 처리

**현재**: 모두 동기 처리

**개선 제안**:
```java
@Async
public CompletableFuture<Void> sendOrderConfirmationEmail(Long orderId) {
    // 이메일 발송 (비동기)
}
```

---

## 11. 개선 권장사항

### 11.1 긴급 (High Priority)

#### 11.1.1 보안 강화
```bash
# 1. 환경 변수로 시크릿 관리
export JWT_SECRET=$(openssl rand -base64 64)
export DB_PASSWORD=$(openssl rand -base64 32)
export MAIL_PASSWORD="앱 비밀번호"

# 2. .env 파일 생성
cat > .env <<EOF
JWT_SECRET=${JWT_SECRET}
DB_PASSWORD=${DB_PASSWORD}
MAIL_PASSWORD=${MAIL_PASSWORD}
EOF

# 3. .gitignore에 추가
echo ".env" >> .gitignore
```

#### 11.1.2 재고 차감 동시성 제어
```java
// ProductService
public void decreaseStock(Long productId, int quantity) {
    RLock lock = redissonClient.getLock("stock:" + productId);
    try {
        lock.lock(5, TimeUnit.SECONDS);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException());

        if (product.getProductStock() < quantity) {
            throw new InsufficientStockException();
        }

        product.setProductStock(product.getProductStock() - quantity);
        productRepository.save(product);

    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

#### 11.1.3 Gateway 필터 추가
```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
```

### 11.2 중요 (Medium Priority)

#### 11.2.1 Redis 캐싱 활성화
```java
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(/* Jackson2JsonRedisSerializer */);

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}

// ProductService
@Cacheable(value = "products", key = "#productId")
public Product getProductById(Long productId) {
    return productRepository.findById(productId).orElseThrow(...);
}
```

#### 11.2.2 N+1 쿼리 해결
```java
// OrderRepository
@Query("SELECT DISTINCT o FROM Order o " +
       "LEFT JOIN FETCH o.orderItems " +
       "WHERE o.userId = :userId")
List<Order> findAllByUserIdWithItems(@Param("userId") Long userId);
```

#### 11.2.3 인덱스 추가
```sql
-- V1__add_performance_indexes.sql
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_status ON orders(order_status);
CREATE INDEX idx_order_date ON orders(order_date);
CREATE INDEX idx_orderitem_status ON order_items(order_item_status);
CREATE INDEX idx_product_type ON product(product_type);
CREATE INDEX idx_product_stock ON product(product_stock);
```

### 11.3 선택 (Low Priority)

#### 11.3.1 이벤트 기반 아키텍처
```java
// 주문 생성 시 이벤트 발행
applicationEventPublisher.publishEvent(new OrderCreatedEvent(order));

// 이메일 서비스에서 비동기 처리
@EventListener
@Async
public void handleOrderCreated(OrderCreatedEvent event) {
    emailService.sendOrderConfirmation(event.getOrder());
}
```

#### 11.3.2 Circuit Breaker
```java
@CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProduct")
public Product getProduct(Long productId) {
    return webClient.get()
        .uri("http://product:8082/api/products/" + productId)
        .retrieve()
        .bodyToMono(Product.class)
        .block();
}

public Product fallbackGetProduct(Long productId, Exception e) {
    return Product.builder()
        .productName("일시적으로 사용 불가")
        .build();
}
```

#### 11.3.3 분산 추적
```gradle
implementation 'io.micrometer:micrometer-tracing-bridge-brave'
implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
```

```properties
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
```

### 11.4 아키텍처 개선 로드맵

```
Phase 1 (1-2주): 긴급 보안 패치
├─ Secret Key 외부화
├─ 재고 차감 동시성 제어
└─ Gateway JWT 필터

Phase 2 (2-4주): 성능 최적화
├─ Redis 캐싱 활성화
├─ N+1 쿼리 해결
├─ 인덱스 추가
└─ 쿼리 최적화

Phase 3 (1-2개월): 고급 기능
├─ Circuit Breaker
├─ 분산 추적
├─ 이벤트 기반 아키텍처
└─ Service Mesh (Istio)

Phase 4 (3-6개월): 클라우드 네이티브
├─ Kubernetes 마이그레이션
├─ DB per Service 패턴
├─ 메시지 큐 (Kafka/RabbitMQ)
└─ Saga 패턴 (분산 트랜잭션)
```

---

## 12. 결론

### 12.1 프로젝트 종합 평가

#### 12.1.1 강점 (Strengths)

1. **명확한 MSA 전환**
   - 서비스별 명확한 책임 분리
   - Docker Compose 기반 컨테이너화
   - 독립적 배포 가능

2. **효과적인 동시성 제어**
   - 낙관적 락 도입으로 성능 15-40% 개선
   - 분산락 대안 보유 (유연성)
   - 실제 성능 테스트 수행 및 최적화

3. **완성도 높은 도메인 모델**
   - 명확한 상태 머신 (Order, OrderItem)
   - JPA 연관 관계 잘 구현
   - DTO 변환 패턴 적용

4. **자동화**
   - 배송 상태 스케줄러
   - 이메일 인증 자동화

5. **코드 구조**
   - 레이어드 아키텍처 준수
   - 생성자 주입 패턴
   - Custom Exception 정의

#### 12.1.2 약점 (Weaknesses)

1. **보안 취약점**
   - Secret Key, DB 비밀번호 평문 노출
   - JWT 블랙리스트 미구현
   - HTTPS 미적용

2. **Redis 미활용**
   - 캐싱 미구현
   - 세션 관리 미구현
   - 분산락 비활성화

3. **Product Service 동시성**
   - 재고 차감 시 경합 조건 가능
   - 동시성 제어 부재

4. **모니터링 부재**
   - 로그 수집 시스템 없음
   - 메트릭 수집 없음
   - 분산 추적 없음

5. **테스트 부족**
   - 단위 테스트 확인 불가
   - 통합 테스트 부재

#### 12.1.3 기회 (Opportunities)

1. **성능 최적화**
   - Redis 캐싱으로 5-10배 성능 향상 가능
   - 인덱스 추가로 쿼리 성능 개선
   - 비동기 처리 도입

2. **확장성**
   - Kubernetes 마이그레이션
   - Read Replica 도입
   - 메시지 큐 도입

3. **운영 효율성**
   - CI/CD 파이프라인
   - 자동화된 테스트
   - 모니터링 대시보드

#### 12.1.4 위협 (Threats)

1. **보안 침해**
   - 노출된 Secret Key 악용
   - DDoS 공격 취약

2. **데이터 정합성**
   - 재고 차감 시 동시성 이슈
   - 분산 트랜잭션 미구현

3. **운영 리스크**
   - 단일 장애 지점 (MySQL, Redis)
   - 장애 감지 지연

### 12.2 프로젝트 성숙도 평가

| 영역 | 점수 | 평가 |
|------|------|------|
| **아키텍처** | 7/10 | MSA 전환 성공, 서비스 디스커버리 부족 |
| **코드 품질** | 7/10 | 구조 양호, 테스트 부족 |
| **보안** | 4/10 | 기본 기능, 많은 취약점 |
| **성능** | 8/10 | 동시성 최적화 우수, 캐싱 미활용 |
| **확장성** | 6/10 | 수평 확장 가능, DB 병목 존재 |
| **운영성** | 5/10 | 컨테이너화 완료, 모니터링 부족 |
| **문서화** | 8/10 | README 우수, API 문서 외부 |
| **전체** | **6.4/10** | **양호 (개선 여지 많음)** |

### 12.3 최종 권장사항

#### 12.3.1 즉시 조치 (1주 내)
1. ✅ Secret Key, 비밀번호 환경 변수화
2. ✅ 재고 차감 동시성 제어 추가
3. ✅ Gateway JWT 필터 구현

#### 12.3.2 단기 개선 (1개월 내)
1. ✅ Redis 캐싱 활성화
2. ✅ N+1 쿼리 해결
3. ✅ 인덱스 추가
4. ✅ 단위 테스트 작성

#### 12.3.3 중기 개선 (3개월 내)
1. ✅ 모니터링 시스템 구축 (Prometheus + Grafana)
2. ✅ CI/CD 파이프라인
3. ✅ 이벤트 기반 아키텍처 일부 도입
4. ✅ Service Mesh 검토

#### 12.3.4 장기 개선 (6개월 이상)
1. ✅ Kubernetes 마이그레이션
2. ✅ DB per Service 패턴
3. ✅ Saga 패턴 (분산 트랜잭션)
4. ✅ 완전한 이벤트 기반 아키텍처

### 12.4 결론

BuyMe 프로젝트는 **MSA 전환의 성공적인 사례**로, 특히 **동시성 제어 최적화**에서 뛰어난 성과를 보였습니다. 분산락에서 낙관적 락으로의 전환을 통해 15-40%의 성능 향상을 달성한 것은 주목할 만합니다.

그러나 **보안, Redis 활용, 모니터링** 영역에서는 프로덕션 환경으로 가기 위한 추가 작업이 필요합니다. 특히 민감 정보 노출과 재고 차감 동시성 이슈는 우선적으로 해결되어야 합니다.

전반적으로 이 프로젝트는 **견고한 기반 위에 구축**되었으며, 제시된 개선사항을 단계적으로 적용한다면 **프로덕션 레벨의 MSA 시스템**으로 발전할 수 있는 큰 잠재력을 가지고 있습니다.

---

## 부록

### A. 참조 문서
- README.md
- [API 명세서](https://www.notion.so/API-82e0878c996347ed8367bb808b7975de?pvs=21)
- [ERD](https://www.notion.so/ERD-f4ef0ae9edfe4e29a84a65eed178f80f?pvs=21)
- [트러블슈팅 블로그](https://dilution0216.tistory.com/294)

### B. 분석에 사용된 도구
- 코드 리뷰: 수동 분석
- 아키텍처 다이어그램: ASCII Art
- ERD: 코드 기반 추론

### C. 분석 범위
- 소스 코드: Gateway, User, Product, Order Service
- 설정 파일: application.properties, build.gradle, docker-compose.yml
- 문서: README.md, Notion (외부 링크)

### D. 분석 제외 사항
- 실제 테스트 코드 (파일 미제공)
- JMeter 상세 설정 (test.jmx 바이너리 파일)
- 프론트엔드 코드 (존재 여부 불명)

---

**보고서 작성**: Claude AI (Sonnet 4.5)
**검토**: 필요
**다음 단계**: 개선 권장사항 구현 계획 수립

---

**이 보고서는 BuyMe 프로젝트의 현재 상태를 종합적으로 분석하고, 프로덕션 환경으로의 전환을 위한 구체적인 로드맵을 제시합니다.**
