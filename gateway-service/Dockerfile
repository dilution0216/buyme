# Java 21을 기반으로 하는 이미지 선택
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY /build/libs/*.jar /app/gateway-service.jar
ENTRYPOINT ["java", "-jar", "/app/gateway-service.jar"]