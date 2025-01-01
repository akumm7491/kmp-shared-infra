FROM gradle:8.5-jdk17 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle :examples:template-service:shadowJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8082

COPY --from=builder /home/gradle/src/examples/template-service/build/libs/template-service.jar /app/template-service.jar

ENTRYPOINT ["java", "-jar", "/app/template-service.jar"]
