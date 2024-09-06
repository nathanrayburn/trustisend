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

# Copy the jar file generated during the build to the runtime image
COPY --from=build /app/target/*.jar ./app.jar

# Copy the keys folder to the root directory (/)
COPY ./keys /keys

# Expose the default port used by Spring Boot
EXPOSE 8080 8000

# JVM Memory Optimization for Spring Boot
# -XX:+UseContainerSupport: Ensures that JVM optimizes memory usage based on container limits.
# -XX:MaxRAMPercentage=75.0: Limits the JVM heap size to 75% of the container's memory limit.
# -XshowSettings:vm: Shows the JVM settings at startup.

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XshowSettings:vm", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "./app.jar"]
