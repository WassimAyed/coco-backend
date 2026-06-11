# Multi-service build for the API Gateway as the core backend component
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Build the API Gateway (assuming it's the core component)
COPY apiGateway/pom.xml ./apiGateway/
RUN mvn -f apiGateway/pom.xml dependency:go-offline
COPY apiGateway/src ./apiGateway/src
RUN mvn -f apiGateway/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/apiGateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
