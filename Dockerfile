# Use the official Oracle OpenJDK 22 image for aarch64
FROM openjdk:22-jdk-oraclelinux8

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the host to the container
COPY target/jedis-client-side-caching-redis-0.1.0-gabs-jar-with-dependencies.jar /app/app.jar

# Specify the command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
