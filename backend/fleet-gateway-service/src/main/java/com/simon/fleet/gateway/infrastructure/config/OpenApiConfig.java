package com.simon.fleet.gateway.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Fleet Gateway Service API")
                .description("Registro de vehículos y orquestador del Saga de eliminación")
                .version("v1"));
    }
}
