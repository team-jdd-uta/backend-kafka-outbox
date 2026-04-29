FROM gradle:9.4.1-jdk21 AS build

WORKDIR /workspace

COPY build.gradle settings.gradle ./
COPY shared-events ./shared-events
COPY user-service ./user-service
COPY notification-service ./notification-service

RUN gradle :notification-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/notification-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]