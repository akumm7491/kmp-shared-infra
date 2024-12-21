FROM gradle:7.6.1-jdk17 as builder

WORKDIR /app
COPY . .
RUN gradle :examples:weather-service:build -x test

FROM openjdk:17-slim

WORKDIR /app
COPY --from=builder /app/examples/weather-service/build/libs/*.jar ./weather-service.jar

# Default to producer service
ENV SERVICE_TYPE=producer

EXPOSE 8084 8085

CMD if [ "$SERVICE_TYPE" = "consumer" ]; then \
      java -cp weather-service.jar com.example.kmp.weather.ConsumerApplicationKt; \
    else \
      java -cp weather-service.jar com.example.kmp.weather.ApplicationKt; \
    fi
