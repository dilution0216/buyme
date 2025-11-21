# BuyMe 프로젝트 전반적 분석 계획서

## 1. 분석 개요

### 1.1 프로젝트 요약
- **프로젝트명**: BuyMe - 선착순 구매 서비스
- **아키텍처**: MSA (Microservices Architecture)
- **주요 기술**: Spring Boot 3.3.2, MySQL 8.0, Redis, Docker Compose
- **특징**: 대규모 동시 트래픽 처리, 분산 환경에서의 동시성 제어

### 1.2 분석 목적
- MSA 전환 후 아키텍처 구조 파악
- 동시성 제어 메커니즘 (낙관적 락, 분산락) 분석
- 대규모 트래픽 처리 성능 최적화 방안 분석
- 코드 품질 및 개선 가능성 도출
- 보안, 예외처리, 로깅 전략 검토

### 1.3 분석 범위
- 4개 마이크로서비스 (Gateway, User, Product, Order)
- 데이터베이스 스키마 및 연동 구조
- Redis 활용 전략 (캐싱, 분산락)
- 컨테이너 오케스트레이션 (Docker Compose)
- 성능 테스트 설정 (JMeter)

---

## 2. 프로젝트 구조 분석

### 2.1 전체 아키텍처
```
buyme/
├── gateway-service/          # API Gateway (Port 8080)
├── user-service/            # 사용자 관리 서비스 (Port 8081)
├── product-service/         # 상품 관리 서비스 (Port 8082)
├── order-service/           # 주문 관리 서비스 (Port 8083)
├── app/                     # 레거시 모노리스 (참고용)
├── docker-compose.yml       # 컨테이너 오케스트레이션
├── test.jmx                 # JMeter 부하 테스트 설정
└── build.gradle             # 멀티 모듈 Gradle 설정
```

### 2.2 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot 3.3.2
- **Database**: MySQL 8.0
- **Cache/Lock**: Redis (latest)
- **Security**: JWT (jjwt 0.11.5)
- **Container**: Docker, Docker Compose
- **Build Tool**: Gradle
- **Testing**: JMeter, JUnit

### 2.3 서비스 간 통신 구조
```
Client → Gateway (8080) → {
    User Service (8081)
    Product Service (8082)
    Order Service (8083)
} → MySQL (3306) / Redis (6379)
```

---

## 3. 상세 분석 항목

### 3.1 Gateway Service 분석
**분석 대상**:
- [ ] API 라우팅 규칙 및 매핑 구조
- [ ] 필터 체인 및 인터셉터 구현
- [ ] 로드 밸런싱 전략
- [ ] CORS 설정
- [ ] 에러 핸들링 및 응답 표준화
- [ ] 서비스 디스커버리 메커니즘

**분석 방법**:
- `gateway-service/src/main/java` 패키지 구조 분석
- `application.properties` 설정 파일 검토
- 라우팅 로직 및 필터 구현 코드 리뷰

**예상 산출물**:
- Gateway 라우팅 맵 문서
- 필터 체인 플로우차트
- 설정 최적화 제안서

---

### 3.2 User Service 분석
**분석 대상**:
- [ ] 사용자 엔티티 및 도메인 모델 (`User.java`)
- [ ] 인증/인가 메커니즘 (`SecurityConfig.java`, `JwtTokenProvider.java`)
- [ ] 암호화 전략 (`EncryptionUtil.java`)
- [ ] 예외 처리 구조 (`GlobalExceptionHandler.java`)
- [ ] 이메일/전화번호 검증 로직
- [ ] 회원가입/로그인/정보수정 API
- [ ] 보안 취약점 분석

**분석 방법**:
- Entity, Repository, Service, Controller 레이어 분석
- JWT 토큰 생성/검증 로직 검토
- 암호화 알고리즘 및 키 관리 방식 확인
- 예외 처리 패턴 분석

**예상 산출물**:
- 사용자 도메인 ERD
- JWT 인증 플로우 다이어그램
- 보안 취약점 체크리스트
- API 명세서

---

### 3.3 Product Service 분석
**분석 대상**:
- [ ] 상품 엔티티 및 위시리스트 구조 (`Product.java`, `WishList.java`)
- [ ] 재고 관리 로직
- [ ] Redis 캐싱 전략 (`RedisConfig.java`)
- [ ] 상품 CRUD API
- [ ] 재고 확인 및 차감 메커니즘
- [ ] 위시리스트 관리 기능
- [ ] 동시성 제어 (재고 차감 시)

**분석 방법**:
- 상품 도메인 모델 및 비즈니스 로직 분석
- Redis 캐시 키 전략 및 TTL 설정 확인
- 재고 차감 시 동시성 처리 방식 검토
- ProductService, WishListService 로직 리뷰

**예상 산출물**:
- 상품 도메인 ERD
- Redis 캐싱 전략 문서
- 재고 관리 플로우차트
- API 명세서

---

### 3.4 Order Service 분석 (핵심)
**분석 대상**:
- [ ] 주문 엔티티 및 주문 아이템 구조 (`Order.java`, `OrderItem.java`)
- [ ] 주문 생성/취소/반품 비즈니스 로직
- [ ] 결제 처리 메커니즘
- [ ] 배송 상태 스케줄러 (`OrderStatusScheduler.java`)
- [ ] Redis 기반 분산락 (`RedissonConfig.java`)
- [ ] 낙관적 락 구현 (Optimistic Lock)
- [ ] 동시성 제어 전략 비교 (분산락 vs 낙관적 락)
- [ ] 주문 상태 머신 (State Machine)
- [ ] 재고 확인 및 차감 연동

**분석 방법**:
- OrderService 핵심 비즈니스 로직 상세 분석
- Redisson 분산락 구현 코드 리뷰
- JPA Optimistic Lock 적용 부분 확인
- 스케줄러를 통한 배송 상태 자동 업데이트 로직 분석
- 트랜잭션 경계 및 격리 수준 검토

**예상 산출물**:
- 주문 도메인 ERD
- 주문 생성 플로우차트 (동시성 제어 포함)
- 분산락 vs 낙관적 락 성능 비교 분석
- 상태 전이 다이어그램
- API 명세서

---

### 3.5 데이터베이스 스키마 및 연동 분석
**분석 대상**:
- [ ] MySQL 스키마 구조
- [ ] 각 서비스별 테이블 및 관계
- [ ] 인덱스 전략
- [ ] JPA Entity 매핑 구조
- [ ] 연관 관계 (@OneToMany, @ManyToOne 등)
- [ ] 쿼리 최적화 (N+1 문제, Fetch Join 등)
- [ ] 트랜잭션 관리 전략

**분석 방법**:
- Entity 클래스 분석
- application.properties의 JPA 설정 확인
- Repository 쿼리 메소드 및 JPQL/Native Query 검토
- 데이터베이스 마이그레이션 전략 확인

**예상 산출물**:
- 전체 ERD (통합)
- 인덱스 전략 문서
- 쿼리 최적화 체크리스트
- 트랜잭션 범위 다이어그램

---

### 3.6 Redis 활용 분석
**분석 대상**:
- [ ] Redis 설정 (`RedisConfig.java`)
- [ ] Redisson 분산락 설정 (`RedissonConfig.java`)
- [ ] 캐싱 전략 (Cache-Aside, Write-Through 등)
- [ ] 캐시 키 네이밍 규칙
- [ ] TTL 설정 전략
- [ ] 분산락 획득/해제 로직
- [ ] 락 타임아웃 및 재시도 메커니즘
- [ ] Redis 데이터 구조 활용 (String, Hash, Set 등)

**분석 방법**:
- RedisConfig, RedissonConfig 설정 파일 분석
- @Cacheable, @CachePut, @CacheEvict 어노테이션 사용 확인
- Redisson Lock API 사용 코드 리뷰
- docker-compose.yml의 Redis 설정 검토

**예상 산출물**:
- Redis 아키텍처 다이어그램
- 캐싱 전략 문서
- 분산락 메커니즘 상세 설명서
- 성능 튜닝 가이드

---

### 3.7 동시성 제어 메커니즘 분석
**분석 대상**:
- [ ] 낙관적 락 (Optimistic Lock) 구현
  - @Version 어노테이션 사용 확인
  - 재시도 로직 (OptimisticLockingFailureException 처리)
- [ ] 분산락 (Distributed Lock) 구현
  - Redisson Lock API 사용
  - 락 획득 타임아웃 설정
  - 락 리스 타임 설정
- [ ] 비관적 락 (Pessimistic Lock) 사용 여부
- [ ] 동시성 이슈 시나리오별 처리 방식
  - 재고 차감
  - 주문 생성
  - 결제 처리

**분석 방법**:
- 동시성 제어가 필요한 코드 섹션 식별
- 각 락 메커니즘의 구현 코드 상세 분석
- 성능 트레이드오프 분석 (README의 트러블슈팅 참고)
- 경합 조건 (Race Condition) 발생 가능성 검토

**예상 산출물**:
- 동시성 제어 전략 비교표
- 락 메커니즘별 플로우차트
- 성능 테스트 결과 분석 (README 참조)
- 동시성 이슈 해결 가이드

---

### 3.8 Docker Compose 및 컨테이너 구성 분석
**분석 대상**:
- [ ] docker-compose.yml 서비스 정의
- [ ] 네트워크 구성
- [ ] 볼륨 마운트 전략
- [ ] 환경 변수 관리
- [ ] 헬스체크 설정
- [ ] 서비스 간 의존성 (depends_on)
- [ ] 각 서비스별 Dockerfile
- [ ] 리소스 제한 설정
- [ ] 로그 관리 전략

**분석 방법**:
- docker-compose.yml 상세 분석
- 각 서비스의 Dockerfile 검토
- 네트워크 격리 및 통신 경로 확인
- 볼륨 영속성 전략 검토

**예상 산출물**:
- 컨테이너 아키텍처 다이어그램
- Docker Compose 최적화 가이드
- 환경 변수 관리 방안
- 배포 절차서

---

### 3.9 JMeter 부하 테스트 설정 분석
**분석 대상**:
- [ ] test.jmx 테스트 시나리오
- [ ] 스레드 그룹 설정 (동시 사용자 수)
- [ ] 테스트 데이터 설정
- [ ] 성능 측정 지표 (응답시간, 처리량, 에러율)
- [ ] 부하 테스트 시나리오
  - 주문 생성 부하 테스트
  - 결제 처리 부하 테스트
  - 재고 확인 부하 테스트
- [ ] 성능 목표 및 달성도

**분석 방법**:
- test.jmx 파일 분석
- README의 성능 테스트 결과 검토
- 부하 테스트 시나리오별 결과 비교

**예상 산출물**:
- JMeter 테스트 계획 문서
- 성능 테스트 결과 리포트
- 성능 개선 전후 비교 분석
- 성능 튜닝 권장사항

---

### 3.10 API 엔드포인트 및 통신 구조 분석
**분석 대상**:
- [ ] 전체 API 엔드포인트 목록
- [ ] RESTful API 설계 원칙 준수 여부
- [ ] 요청/응답 DTO 구조
- [ ] API 버저닝 전략
- [ ] 서비스 간 통신 방식 (REST, 동기/비동기)
- [ ] 에러 응답 표준화
- [ ] API 문서화 (Swagger/OpenAPI 사용 여부)

**분석 방법**:
- Controller 클래스 분석
- DTO 구조 검토
- API 호출 흐름 추적
- Notion API 명세서와 코드 비교

**예상 산출물**:
- API 엔드포인트 전체 목록
- 서비스 간 통신 시퀀스 다이어그램
- API 설계 개선 제안서
- OpenAPI/Swagger 스펙 문서 (생성 시)

---

### 3.11 보안, 예외처리, 로깅 전략 분석
**분석 대상**:
- [ ] **보안**
  - JWT 토큰 생성/검증 로직
  - 비밀번호 암호화 방식
  - HTTPS 설정
  - SQL Injection 방지
  - XSS 방지
  - CSRF 방지
  - 민감 정보 노출 여부 (.env 파일 관리)
- [ ] **예외처리**
  - GlobalExceptionHandler 구조
  - 커스텀 예외 클래스
  - 예외 응답 포맷 표준화
  - 예외 로깅 전략
- [ ] **로깅**
  - 로깅 프레임워크 (Logback, SLF4J)
  - 로그 레벨 설정 (DEBUG, INFO, WARN, ERROR)
  - 구조화된 로깅 (Structured Logging)
  - 민감 정보 마스킹
  - 로그 수집 및 모니터링 도구 연동

**분석 방법**:
- SecurityConfig 및 JwtTokenProvider 코드 리뷰
- GlobalExceptionHandler 패턴 분석
- 로깅 설정 파일 (logback.xml) 검토
- .env 파일 및 환경 변수 관리 확인

**예상 산출물**:
- 보안 취약점 진단 리포트
- 예외처리 가이드라인
- 로깅 전략 문서
- 보안 강화 체크리스트

---

### 3.12 코드 품질 및 개선 사항 도출
**분석 대상**:
- [ ] 코드 스타일 및 컨벤션
- [ ] SOLID 원칙 준수 여부
- [ ] 디자인 패턴 활용
- [ ] 테스트 커버리지
- [ ] 코드 중복 (DRY 원칙)
- [ ] 순환 의존성 (Circular Dependency)
- [ ] 레이어드 아키텍처 준수
- [ ] 성능 병목 지점
- [ ] 기술 부채 (Technical Debt)

**분석 방법**:
- 정적 코드 분석 도구 활용 (SonarQube, Checkstyle 등)
- 코드 리뷰
- 테스트 코드 분석
- 아키텍처 다이어그램 작성

**예상 산출물**:
- 코드 품질 리포트
- 리팩토링 우선순위 목록
- 아키텍처 개선 제안서
- 테스트 전략 개선 방안

---

## 4. 분석 방법론

### 4.1 정적 분석 (Static Analysis)
- 소스 코드 구조 및 패턴 분석
- 설정 파일 검토
- 의존성 분석
- 아키텍처 다이어그램 작성

### 4.2 동적 분석 (Dynamic Analysis)
- 애플리케이션 실행 및 테스트
- API 호출 테스트
- 부하 테스트 수행
- 로그 분석

### 4.3 문서 검토
- README.md 및 관련 문서
- Notion API 명세서, ERD
- 트러블슈팅 및 기술적 의사결정 문서

### 4.4 도구 활용
- IDE (IntelliJ IDEA, VS Code)
- Docker Desktop
- Postman / Insomnia (API 테스트)
- JMeter (부하 테스트)
- Redis CLI
- MySQL Workbench / Adminer

---

## 5. 분석 일정 (예상)

| 단계 | 분석 항목 | 예상 소요 시간 |
|------|-----------|----------------|
| 1 | 프로젝트 구조 및 아키텍처 | 1-2시간 |
| 2 | Gateway Service | 1시간 |
| 3 | User Service | 2시간 |
| 4 | Product Service | 2시간 |
| 5 | Order Service (핵심) | 3-4시간 |
| 6 | 데이터베이스 스키마 | 2시간 |
| 7 | Redis 활용 | 2시간 |
| 8 | 동시성 제어 메커니즘 | 3시간 |
| 9 | Docker Compose | 1시간 |
| 10 | JMeter 부하 테스트 | 2시간 |
| 11 | API 엔드포인트 | 2시간 |
| 12 | 보안/예외/로깅 | 2시간 |
| 13 | 코드 품질 및 개선 | 3시간 |
| 14 | 최종 보고서 작성 | 2-3시간 |
| **총계** | | **28-32시간** |

---

## 6. 분석 산출물

### 6.1 문서
- [ ] 프로젝트 아키텍처 다이어그램
- [ ] 서비스별 상세 분석 리포트
- [ ] 통합 ERD
- [ ] API 명세서 (업데이트)
- [ ] 동시성 제어 전략 비교 분석
- [ ] 성능 테스트 결과 리포트
- [ ] 보안 취약점 진단 리포트
- [ ] 코드 품질 리포트
- [ ] 개선 제안서

### 6.2 다이어그램
- [ ] 시스템 아키텍처 다이어그램
- [ ] 서비스 간 통신 시퀀스 다이어그램
- [ ] ERD (통합)
- [ ] Redis 아키텍처 다이어그램
- [ ] 주문 처리 플로우차트
- [ ] 동시성 제어 플로우차트
- [ ] 컨테이너 아키텍처 다이어그램

### 6.3 코드 개선
- [ ] 코드 리팩토링 제안 목록
- [ ] 성능 최적화 포인트
- [ ] 보안 강화 코드 예제
- [ ] 테스트 코드 개선 방안

---

## 7. 기대 효과

### 7.1 기술적 효과
- MSA 아키텍처의 강점과 약점 명확화
- 동시성 제어 메커니즘에 대한 깊은 이해
- 성능 최적화 방안 도출
- 기술 부채 식별 및 해결 방안 제시

### 7.2 운영적 효과
- 유지보수성 향상
- 확장성 개선 방안 제시
- 장애 대응 능력 강화
- 모니터링 및 로깅 전략 개선

### 7.3 비즈니스 효과
- 서비스 안정성 향상
- 사용자 경험 개선
- 개발 생산성 향상
- 기술적 경쟁력 확보

---

## 8. 주요 분석 포인트 (요약)

### 8.1 핵심 질문
1. MSA 전환이 프로젝트에 적합한가?
2. 동시성 제어 전략 (낙관적 락 vs 분산락)은 효과적인가?
3. 대규모 트래픽 처리를 위한 성능 최적화가 충분한가?
4. 보안 취약점은 없는가?
5. 코드 품질은 유지보수 가능한 수준인가?
6. 테스트 전략은 충분한가?

### 8.2 위험 요소
- 서비스 간 강한 결합 (Tight Coupling)
- 분산 트랜잭션 관리의 복잡성
- 동시성 제어 실패 시 데이터 불일치
- 성능 저하 지점
- 보안 취약점

### 8.3 개선 기회
- API Gateway 고도화
- 이벤트 기반 아키텍처 도입
- Circuit Breaker 패턴 적용
- 분산 추적 (Distributed Tracing) 도입
- 모니터링 및 알림 시스템 강화

---

## 9. 참고 자료

- [API 명세서](https://www.notion.so/API-82e0878c996347ed8367bb808b7975de?pvs=21)
- [ERD](https://www.notion.so/ERD-f4ef0ae9edfe4e29a84a65eed178f80f?pvs=21)
- [트러블슈팅 블로그](https://dilution0216.tistory.com/294)
- README.md
- docker-compose.yml
- build.gradle

---

## 10. 분석 체크리스트

### Phase 1: 구조 파악
- [x] 프로젝트 전체 구조 확인
- [x] 서비스 목록 및 포트 확인
- [x] 기술 스택 파악
- [ ] 빌드 및 실행 환경 구성

### Phase 2: 서비스별 분석
- [ ] Gateway Service 완료
- [ ] User Service 완료
- [ ] Product Service 완료
- [ ] Order Service 완료

### Phase 3: 인프라 분석
- [ ] 데이터베이스 스키마 완료
- [ ] Redis 구성 완료
- [ ] Docker Compose 완료

### Phase 4: 품질 분석
- [ ] 동시성 제어 분석 완료
- [ ] 성능 테스트 분석 완료
- [ ] 보안 분석 완료
- [ ] 코드 품질 분석 완료

### Phase 5: 문서화
- [ ] 다이어그램 작성 완료
- [ ] 분석 리포트 작성 완료
- [ ] 개선 제안서 작성 완료

---

## 11. 연락 및 문의

분석 과정에서 추가 정보가 필요하거나 질문이 있을 경우:
- GitHub Repository 확인
- Notion 문서 참조
- 기술 블로그 (https://dilution0216.tistory.com/) 참조

---

**작성일**: 2025-11-21
**분석 시작일**: TBD
**예상 완료일**: TBD

---

## 부록: 분석 템플릿

각 서비스 분석 시 다음 템플릿을 활용:

### 서비스 분석 템플릿
1. **개요**
   - 서비스명, 역할, 포트
2. **구조**
   - 패키지 구조
   - 주요 클래스 목록
3. **도메인 모델**
   - Entity 구조
   - 연관 관계
4. **비즈니스 로직**
   - 주요 기능
   - 트랜잭션 범위
5. **API**
   - 엔드포인트 목록
   - 요청/응답 구조
6. **의존성**
   - 외부 서비스 호출
   - 데이터베이스 연동
   - Redis 사용
7. **설정**
   - application.properties
   - 환경 변수
8. **문제점 및 개선사항**
   - 발견된 이슈
   - 개선 제안
