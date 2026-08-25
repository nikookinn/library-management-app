# syntax=docker/dockerfile:1

# Stage 1: dependency resolution (cache-friendly layer)
FROM eclipse-temurin:26-jdk-alpine AS deps
WORKDIR /app

COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew \
    && ./gradlew dependencies --no-daemon

# Stage 2: build the application
FROM deps AS build

COPY src src

RUN ./gradlew bootJar --no-daemon -x test \
    && mv build/libs/*.jar build/libs/app.jar

# Stage 3: minimal runtime image
FROM eclipse-temurin:26-jre-alpine AS runtime

LABEL org.opencontainers.image.title="library-management-application" \
      org.opencontainers.image.description="Library Management Application - Spring Boot REST API" \
      org.opencontainers.image.licenses="NOASSERTION"

RUN addgroup -g 10001 spring \
    && adduser -D -H -u 10001 -G spring spring

WORKDIR /app

RUN mkdir -p /app/uploads \
    && chown -R spring:spring /app

COPY --from=build --chown=spring:spring /app/build/libs/app.jar ./app.jar

USER spring:spring

EXPOSE 8080

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError" \
    UPLOAD_DIR="/app/uploads"

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
    CMD wget -q -O- http://127.0.0.1:8080/actuator/health/readiness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
