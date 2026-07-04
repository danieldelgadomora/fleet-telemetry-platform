package com.simon.fleet.ingestion.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expone Swagger UI en {@code /swagger-ui.html} para poder probar la ingesta de telemetría
 * directamente contra el backend, sin depender del dashboard ni del simulador.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ingestionServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Ingestion Service API")
                .description("Servicio de Ingesta (Geolocalización) del sistema de telemetría de flotas")
                .version("v1"));
    }
}
