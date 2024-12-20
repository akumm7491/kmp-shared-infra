FROM gradle:8.5-jdk17 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle :examples:service-b:buildFatJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8082

COPY --from=builder /home/gradle/src/examples/service-b/build/libs/service-b-all.jar /app/service-b.jar

ENTRYPOINT ["java", "-jar", "/app/service-b.jar"]
