# Step 1: Use the official Maven image to build the Spring Boot app with Java 21
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and app directory (which contains your Spring Boot project) to the container
COPY ./dev/test-gc-storage/springboot-gcs-demo-master/pom.xml ./pom.xml
COPY ./dev/test-gc-storage/springboot-gcs-demo-master/src ./src

# Run package the application
RUN mvn clean package -DskipTests

# Declare the build argument
ARG GC_STORAGE_KEY

# Step 2: Use the official Java 21 runtime image to run the Spring Boot app
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file generated during the build to the runtime image
COPY --from=build /dev/test-gc-storage/springboot-gcs-demo-master/target/*.jar ./app.jar

# Optionally, write the GC_STORAGE_KEY to a file inside the container
RUN echo "$GC_STORAGE_KEY" > /app/private-key.json

# Expose the default port used by Spring Boot
EXPOSE 8080

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "./app.jar"]
