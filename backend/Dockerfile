# Multi-stage Dockerfile for Spring Boot Backend
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application skipping tests for speed
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/govshield-1.0.0.jar app.jar

# Render & Railway dynamically inject PORT environment variable
ENV PORT=8080
EXPOSE ${PORT}

# Run Spring Boot jar
ENTRYPOINT ["java", "-jar", "app.jar"]
