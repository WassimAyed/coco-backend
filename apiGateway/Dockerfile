# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk-alpine
# Sécurité (DevSecOps) : met à jour les paquets système pour corriger les CVE détectées par Trivy.
RUN apk upgrade --no-cache
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "app.jar"]
