# Use official Maven image with Java 17 to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and install dependencies first (caches this step)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the actual source code and build it
COPY src ./src
RUN mvn clean package -DskipTests

# Use a lightweight JRE image for runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
