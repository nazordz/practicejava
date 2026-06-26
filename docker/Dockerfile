# syntax=docker/dockerfile:1

# ---------- Stage 1: builder (JDK 25 Alpine) ----------
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and build descriptor first for better layer caching.
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

# Pre-download dependencies (cached unless pom.xml changes).
RUN chmod +x ./mvnw \
    && ./mvnw -B dependency:go-offline

# Copy sources and build the executable jar (tests run in CI, not the image).
COPY src ./src
RUN ./mvnw -B clean package -DskipTests \
    && cp target/practicejava-*.jar app.jar

# ---------- Stage 2: runner (JRE 25 Alpine) ----------
FROM eclipse-temurin:25-jre-alpine AS runner

WORKDIR /app

# Run as an unprivileged user.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/app.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
