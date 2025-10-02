# Multi-stage build for better caching and smaller image
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Copy gradle files first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code and build
COPY src src
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy only the built jar
COPY --from=builder /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]