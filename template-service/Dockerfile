FROM gradle:8.5-jdk17 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle :template-service:buildFatJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8080

COPY --from=builder /home/gradle/src/template-service/build/libs/template-service-all.jar /app/template-service.jar

ENTRYPOINT ["java", "-jar", "/app/template-service.jar"]
