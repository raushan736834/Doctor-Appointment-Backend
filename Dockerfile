# Use a base image with Java installed
FROM openjdk:21-jdk-slim

# Add metadata (optional)
LABEL maintainer="raushan736834@gmail.com"

# Create a directory inside the container
WORKDIR /app

# Copy the jar file into the container
COPY target/Doctor-Appointment-0.0.1-SNAPSHOT.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
