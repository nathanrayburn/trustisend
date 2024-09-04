# Step 1: Use the official Maven image to build the Spring Boot app with Java 21
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and app directory (which contains your Spring Boot project) to the container
COPY ./app/pom.xml ./pom.xml
COPY ./app/src ./src

# Run tests and package the application
RUN mvn clean package -DskipTests

# Step 2: Use the official Java 21 runtime image to run the Spring Boot app
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Create a temporary directory for file uploads and downloads
RUN mkdir -p /app/tmp && chmod 777 /app/tmp

# Set Java options, including the temp directory
ENV JAVA_OPTS="-Djava.io.tmpdir=/app/tmp"

# Copy the jar file generated during the build to the runtime image
COPY --from=build /app/target/*.jar ./app.jar

# Expose the default port used by Spring Boot
EXPOSE 8080 8000

# Define the entry point for the container
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar ./app.jar"]
