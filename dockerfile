FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application using bootJar (fixes executable jar issue)
RUN ./mvnw clean bootJar -DskipTests

# Create the final image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy only the executable jar (not plain jar)
COPY --from=0 /app/target/*[!plain].jar app.jar

# Expose port
EXPOSE 8080

# Run with Railway-specific settings
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
