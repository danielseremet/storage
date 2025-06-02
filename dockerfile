FROM eclipse-temurin:17-jdk-alpine

WORKDIR /StorageService

COPY target/storage-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
