FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY contracts/pom.xml contracts/
COPY ingestion-service/pom.xml ingestion-service/
COPY alerting-service/pom.xml alerting-service/
COPY fleet-gateway-service/pom.xml fleet-gateway-service/
RUN mvn -B -pl alerting-service -am dependency:go-offline

COPY contracts/src contracts/src
COPY alerting-service/src alerting-service/src
RUN mvn -B -pl alerting-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/alerting-service/target/alerting-service-*.jar app.jar

EXPOSE 8082

# Sin HEALTHCHECK HTTP a propósito: alerting-service es 100% event-driven (sin
# spring-boot-starter-web, ver README), así que no hay servidor embebido escuchando en 8082
# para que un wget/curl pueda consultar /actuator/health.
ENTRYPOINT ["java", "-jar", "app.jar"]
