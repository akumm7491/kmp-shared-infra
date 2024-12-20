FROM gradle:8.5-jdk17 as builder

WORKDIR /home/gradle/src
COPY . .

RUN gradle microservices:monitoring-service:buildFatJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8083

COPY --from=builder /home/gradle/src/microservices/monitoring-service/build/libs/monitoring-service.jar /app/service.jar

ENTRYPOINT ["java", "-jar", "/app/service.jar"] 