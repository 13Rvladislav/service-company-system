FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/request-service-1.0.0.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]