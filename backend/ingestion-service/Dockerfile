FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY contracts/pom.xml contracts/
COPY ingestion-service/pom.xml ingestion-service/
COPY alerting-service/pom.xml alerting-service/
COPY fleet-gateway-service/pom.xml fleet-gateway-service/
RUN mvn -B -pl ingestion-service -am dependency:go-offline

COPY contracts/src contracts/src
COPY ingestion-service/src ingestion-service/src
RUN mvn -B -pl ingestion-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/ingestion-service/target/ingestion-service-*.jar app.jar

EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=10 \
  CMD wget --spider -q http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
