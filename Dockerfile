# ---------- Stage 1: Builder (optional but good practice) ----------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy jar produced by mvn
COPY target/order-service-0.0.1-SNAPSHOT.jar app.jar


# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user (important for Kubernetes security)
RUN addgroup -S spring && adduser -S spring -G spring

USER spring:spring

# Copy jar from builder stage
COPY --from=builder /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]