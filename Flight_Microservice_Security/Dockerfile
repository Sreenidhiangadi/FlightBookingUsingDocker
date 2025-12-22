FROM eclipse-temurin:17-jre

WORKDIR /flight-microservice

COPY target/Flight_Microservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar","--spring.profiles.active=docker"]
