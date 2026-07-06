package com.simon.fleet.ingestion.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Habilita CORS para el dashboard: el nuevo historial de telemetría (`GET
 * /api/v1/telemetry/{plate}/history`) lo llama directamente la SPA de Angular, a diferencia de
 * `POST /api/v1/telemetry` y `/api/v1/panic`, cuyos únicos clientes hasta ahora (simulador, app
 * móvil) no son navegador y nunca necesitaron esta cabecera.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}
