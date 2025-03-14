# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy as builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .
COPY src ./src

# Build the application using Maven
RUN ./mvnw clean package -DskipTests

# Use a smaller base image for the final stage
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar ./auth-service.jar

# Expose the port the application will run on
EXPOSE 8001

# Set environment variables (optional, can also be passed at runtime)
ENV SPRING_APPLICATION_NAME=auth-service
ENV SERVER_PORT=8001

# Run the application
ENTRYPOINT ["java", "-jar", "auth-service.jar"]