# =============================================================================
# Tempest OMS - Dockerfile
# =============================================================================
# Lightweight runtime image using pre-built JAR.
# Build the JAR first with: ./mvnw clean package -DskipTests
# =============================================================================

FROM eclipse-temurin:21-jre-alpine

# Add wget for health checks
RUN apk add --no-cache wget

WORKDIR /app

# Copy the pre-built JAR
COPY target/oms-*.jar app.jar

# Expose the service port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

