# Build stage - JDK 25
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /build

# Copy Maven wrapper and build files first (better layer caching)
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

# Make wrapper executable and pre-download dependencies (cached unless pom.xml changes)
RUN chmod +x ./mvnw \
    && ./mvnw dependency:go-offline -B

# Copy sources and build the executable jar (skip tests in the image build)
COPY src ./src
RUN ./mvnw -B -DskipTests clean package

# Runtime stage - JRE 25
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy the Spring Boot executable jar from the build stage
COPY --from=build /build/target/*.jar ./app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
