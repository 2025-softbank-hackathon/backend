# 1단계: build
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

# 2단계: run
FROM amazoncorretto:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]