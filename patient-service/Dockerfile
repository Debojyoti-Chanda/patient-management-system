# Stage 1: Build the application
# Use a Maven image that includes the correct Java version for your project.
# ./mvnw --version
FROM maven:3.9-eclipse-temurin-24 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package

FROM openjdk:24-jdk AS runner

WORKDIR /app

COPY --from=builder ./app/target/patient-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4000

ENTRYPOINT ["java","-jar","app.jar"]

#docker build --no-cache  -t patient-service-image:latest .
#to create a clean new image from the project



