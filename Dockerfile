# Java 21을 기반으로 하는 이미지 선택
FROM eclipse-temurin:21-jdk-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 파일과 Gradle Wrapper 스크립트 파일을 복사
COPY build.gradle settings.gradle gradlew gradlew.bat /app/
COPY gradle /app/gradle

# Gradle 종속성을 먼저 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 파일을 복사
COPY src /app/src

# 애플리케이션을 빌드
RUN ./gradlew bootJar --no-daemon

# 실제 실행 환경을 위한 이미지 설정 (Java Runtime Environment)
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 애플리케이션 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 개발 환경으로 설정, 이를 통해 포트 충돌 방지
ENV SPRING_PROFILES_ACTIVE=dev

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]