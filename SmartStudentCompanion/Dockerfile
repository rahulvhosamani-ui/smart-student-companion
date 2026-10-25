# Use a combined Maven and JDK image
FROM maven:3.9.6-eclipse-temurin-17

# Set the working directory inside the container
WORKDIR /app

# Copy your project files
COPY . .

# Build the project package
RUN mvn clean package -DskipTests

# Expose the internal port
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]