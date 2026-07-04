package com.simon.fleet.ingestion.infrastructure.config;

import com.simon.fleet.ingestion.domain.validation.TelemetryValidationChain;
import com.simon.fleet.ingestion.domain.validation.ValidCoordinatesSpecification;
import com.simon.fleet.ingestion.domain.validation.ValidTimestampSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

/**
 * Ensambla la cadena de validación (domain.validation) como beans de Spring. Las clases del
 * dominio no llevan {@code @Component} a propósito; el cableado vive aquí, en infrastructure,
 * para que el dominio siga siendo Java puro. El orden de la lista es el orden en que se evalúa
 * cada regla (Chain of Responsibility).
 */
@Configuration
public class ValidationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TelemetryValidationChain telemetryValidationChain(Clock clock) {
        return new TelemetryValidationChain(List.of(
                new ValidCoordinatesSpecification(),
                new ValidTimestampSpecification(clock)
        ));
    }
}
