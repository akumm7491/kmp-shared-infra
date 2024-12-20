FROM gradle:8.5-jdk17 as builder

WORKDIR /home/gradle/src
COPY . .

RUN gradle microservices:config-server:buildFatJar --no-daemon

FROM openjdk:17-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

EXPOSE 8888

COPY --from=builder /home/gradle/src/microservices/config-server/build/libs/config-server.jar /app/service.jar

ENTRYPOINT ["java", "-jar", "/app/service.jar"] 