# Base image containing Java runtime environment (JDK)
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Information around who maintains the image
LABEL maintainer="microservices-bank.com"

# Copy the jar into image
COPY target/employeehub-0.0.1-SNAPSHOT.jar ./employeehub-0.0.1-SNAPSHOT.jar

# Command to execute on image execution
ENTRYPOINT ["java", "-jar", "employeehub-0.0.1-SNAPSHOT.jar"]

# To build image CLI Command: docker build . -t NAME OF IMAGE (e.g. martinsh121/employeehub:01 )