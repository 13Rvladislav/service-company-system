FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/user-service-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]