FROM gradle:8.5-jdk17 as builder

WORKDIR /app

# Copy the root project files
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle gradle
COPY common-libs common-libs
COPY examples/template-service examples/template-service

# Build the template service
RUN gradle :examples:template-service:shadowJar -x test

FROM openjdk:17-slim

WORKDIR /app
COPY --from=builder /app/examples/template-service/build/libs/*-all.jar ./app.jar

ENV PORT=80
ENV HOST=0.0.0.0

EXPOSE 80

CMD ["java", "-jar", "app.jar"]
