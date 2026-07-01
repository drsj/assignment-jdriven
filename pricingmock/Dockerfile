FROM eclipse-temurin:25-jre

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl

# Copy the jar
COPY target/*.jar app.jar

# Run the app
ENTRYPOINT ["java","-jar","app.jar"]