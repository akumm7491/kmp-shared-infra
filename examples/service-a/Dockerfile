FROM gradle:8.5-jdk17 as builder

WORKDIR /home/gradle/src
COPY . .

RUN gradle :examples:service-a:buildFatJar --no-daemon

FROM openjdk:17-slim

EXPOSE 8081

COPY --from=builder /home/gradle/src/examples/service-a/build/libs/service-a-all.jar /app/service-a.jar

ENTRYPOINT ["java", "-jar", "/app/service-a.jar"]
