FROM gradle:8.5-jdk17 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle :examples:demo-service:buildFatJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8083

COPY --from=builder /home/gradle/src/examples/demo-service/build/libs/demo-service-all.jar /app/demo-service.jar

ENTRYPOINT ["java", "-jar", "/app/demo-service.jar"]
