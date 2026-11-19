FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/template-admin-1.0.jar app.jar

EXPOSE 8084

CMD ["java", "-jar", "/app/app.jar"]