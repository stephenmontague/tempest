# =============================================================================
# Tempest Workflow Worker - Dockerfile
# =============================================================================
# Standalone Temporal workflow worker. No web server, no database.
# Polls the tempest-workflows task queue and executes workflow logic.
# Build the JAR first with: ./mvnw clean package -DskipTests
# =============================================================================

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR
COPY target/workflow-worker-*.jar app.jar

# No port exposed - this is a worker, not a web server

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
