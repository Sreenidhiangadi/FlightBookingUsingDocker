# ===== Stage 1: Build =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package 

# ===== Stage 2: Run =====
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/API_Gateway_Microservices_Security-0.0.1-SNAPSHOT.jar app.jar

# Gateway port
EXPOSE 8765

ENTRYPOINT ["java", "-jar", "app.jar"]
