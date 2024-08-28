# Java 21을 기반으로 하는 이미지 선택
FROM eclipse-temurin:21-jdk-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# 전체 프로젝트 복사
COPY ../../ ./

# Gradle 종속성을 다운로드
RUN ./gradlew build --no-daemon

# 애플리케이션을 빌드
RUN ./gradlew :order-service:bootJar --no-daemon

# 실제 실행 환경을 위한 이미지 설정 (Java Runtime Environment)
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 애플리케이션 JAR 파일을 복사
COPY --from=build /app/order-service/build/libs/*.jar app.jar

# 개발 환경 설정
ENV SPRING_PROFILES_ACTIVE=dev

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
