FROM gradle:8.5-jdk17 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle :microservices:service-a:buildFatJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8081

COPY --from=builder /home/gradle/src/microservices/service-a/build/libs/service-a-all.jar /app/service-a.jar

ENTRYPOINT ["java", "-jar", "/app/service-a.jar"]
