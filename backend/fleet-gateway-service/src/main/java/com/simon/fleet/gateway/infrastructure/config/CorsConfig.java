package com.simon.fleet.gateway.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Habilita CORS para el dashboard: los endpoints de {@code /api/v1/**} se llaman desde un
 * origen distinto (la SPA de Angular), a diferencia del canal WebSocket/STOMP que ya resuelve
 * su propio origen en {@code WebSocketConfig}. Se centraliza aquí en vez de esparcir
 * {@code @CrossOrigin} por cada controlador.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS");
    }
}
