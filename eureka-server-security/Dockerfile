FROM eclipse-temurin:17-jre

WORKDIR /eureka-server

COPY target/eureka-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761

CMD ["java", "-jar", "app.jar","--spring.profiles.active=docker"]
