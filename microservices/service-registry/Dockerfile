FROM gradle:8.5-jdk17 as builder

WORKDIR /home/gradle/src

# Copy the entire project
COPY . .

# Build the service
RUN gradle microservices:service-registry:buildFatJar --no-daemon

FROM openjdk:17-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

EXPOSE 8761

COPY --from=builder /home/gradle/src/microservices/service-registry/build/libs/service-registry.jar /app/service.jar

ENTRYPOINT ["java", "-jar", "/app/service.jar"] 