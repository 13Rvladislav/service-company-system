FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/equipment-service-1.0.0.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]