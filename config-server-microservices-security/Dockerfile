FROM eclipse-temurin:17-jre

WORKDIR /config-server

COPY target/config-server-microservices-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8888

CMD ["java", "-jar", "app.jar"]
