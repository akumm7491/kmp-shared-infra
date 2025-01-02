FROM gradle:8.5-jdk17 AS builder

COPY . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle :examples:template-service:shadowJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8080

COPY --from=builder /home/gradle/src/examples/template-service/build/libs/template-service.jar /app/template-service.jar

ENTRYPOINT ["java", "-jar", "/app/template-service.jar"]
