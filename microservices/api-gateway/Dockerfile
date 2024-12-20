FROM gradle:8.5-jdk17 as builder

WORKDIR /home/gradle/src
COPY . .

RUN gradle microservices:api-gateway:buildFatJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8082

COPY --from=builder /home/gradle/src/microservices/api-gateway/build/libs/api-gateway.jar /app/service.jar

ENTRYPOINT ["java", "-jar", "/app/service.jar"] 