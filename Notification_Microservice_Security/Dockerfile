FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /notification-microservice

COPY target/Notification_Microservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

CMD ["java", "-jar", "app.jar","--spring.profiles.active=docker"]
