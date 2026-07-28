# =============================================================================
# Tempest API - Dockerfile
# =============================================================================
# Unified REST/CRUD API for all domains. Lightweight runtime image using the
# pre-built JAR. Build first with: ./mvnw clean package -DskipTests
# =============================================================================

FROM eclipse-temurin:21-jre-alpine

# Add wget for health checks
RUN apk add --no-cache wget

WORKDIR /app

# Copy the pre-built JAR
COPY target/tempest-api-*.jar app.jar

# Expose the API port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
