FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /user-microservice

COPY target/User_Microservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar","--spring.profiles.active=docker"]
