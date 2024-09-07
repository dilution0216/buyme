## 프로젝트 소개

---

- 수량이 정해져있는 물건을 선착순으로 재고만큼 구매할 수 있게 만든 서비스

## 프로젝트 목표

---

- 모노리스 서비스를 Docker-compose 환경에서 MSA 로 전환
- 약 10000건의 대규모 주문 트래픽 발생 시 Redis 기반 성능 최적화

## 주요 기능

---

### 1. **사용자 관리 (User Service)**

- **회원 가입**: 사용자가 회원으로 가입할 수 있는 기능
- **회원 정보 관리**: 사용자 정보의 CRUD(Create, Read, Update, Delete) 작업 지원

### 2. **상품 관리 (Product Service)**

- **상품 등록**: 새로운 상품을 등록하는 기능
- **상품 정보 수정**: 등록된 상품의 정보를 수정하는 기능
- **상품 목록 조회**: 상품 목록을 조회하는 기능
- **재고 확인**: 특정 상품의 재고 상태를 확인하는 기능

### 3. **주문 관리 (Order Service)**

- **주문 생성**: 사용자가 상품을 선택하여 주문을 생성하는 기능
- **주문 취소**: 사용자가 특정 주문을 취소할 수 있는 기능
- **주문 목록 조회**: 사용자가 자신의 주문 내역을 조회할 수 있는 기능
- **주문 반품 요청**: 사용자가 배송된 상품을 반품 요청하는 기능
- **주문 반품 완료**: 반품 처리가 완료된 주문 항목을 업데이트하는 기능
- **배송 상태 업데이트**: 주문의 배송 상태를 자동으로 업데이트하는 기능. (D+1 상태 변경 → 배송 중, D+2 상태 변경 → 배송 완료)

### 4. **결제 관리 (Payment Service)**

- **결제 시도**: 주문에 대한 결제 시도 기능 (성공/실패 확률을 적용하여 시뮬레이션)
- **결제 성공 처리**: 결제 시도가 성공했을 때 주문 상태를 **PAID**로 변경
- **결제 실패 처리**: 결제 시도가 실패했을 때 주문 상태를 **PAYMENT_FAILED**로 변경

### 5. **재고 확인 (Stock Management)**

- **실시간 재고 확인**: 상품별 재고 상태를 확인하여 재고가 부족할 때 주문을 제한하는 기능

### 6. **동시성 처리 및 락 기능**

- **낙관적 락(Optimistic Lock)**: 충돌 가능성이 낮은 상황에서 성능 최적화를 위해 적용된 락 방식
- **분산락(Redis)**: 분산된 여러 서버에서 동일한 리소스에 대한 동시성 문제를 해결하기 위한 Redis 기반 락 처리
- **락을 적용한 결제 처리**: Redis를 이용한 분산락 또는 낙관적 락을 통해, 결제 처리 시 다중 트랜잭션에서의 동시성 문제 해결

### 7. **부하 테스트 및 성능 최적화**

- **JMeter 부하 테스트**: JMeter를 사용하여 다양한 부하 테스트 시나리오를 설정하고, 대규모 트래픽 상황에서 시스템 성능을 평가
- **락 적용 전/후 성능 테스트**: 분산락과 낙관적 락 적용 전후의 성능 차이를 평가하고, 시스템 병목 현상을 분석하여 최적의 성능을 도출

## 기술 스택

---

- **mysql**
- **Spring Boot**
- **Redis**
- **Docker 및 Docker Compose**
- **JMeter**
  
## API & ERD

---

 [API 명세서](https://www.notion.so/API-82e0878c996347ed8367bb808b7975de?pvs=21) 

[ERD](https://www.notion.so/ERD-f4ef0ae9edfe4e29a84a65eed178f80f?pvs=21) 

## 성능 개선

---

https://dilution0216.tistory.com/294

## 트러블 슈팅

---

MSA 로 구조화 중 발생하는 이슈

https://dilution0216.tistory.com/278

데이터 참조 이슈

https://dilution0216.tistory.com/286

Redis- 락 사용 이슈

https://dilution0216.tistory.com/294

https://dilution0216.tistory.com/291
