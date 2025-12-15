FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package 

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/User_Microservice-0.0.1-SNAPSHOT.jar app.jar

# User port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
