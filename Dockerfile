FROM maven:3.9.6-eclipse-temurin-21 AS build

# Create a directory inside the container
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the jar file into the container
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
