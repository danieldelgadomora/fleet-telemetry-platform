package com.simon.fleet.ingestion.domain.validation;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;

/**
 * Specification Pattern: una regla de negocio independiente que un {@link TelemetryPoint}
 * puede cumplir o no. Cada implementación es un eslabón de la
 * {@link TelemetryValidationChain} (Chain of Responsibility) y puede probarse de forma
 * aislada, sin depender de las demás reglas ni de Spring.
 */
public interface TelemetrySpecification {

    /**
     * @return true si el punto cumple esta regla de negocio.
     */
    boolean isSatisfiedBy(TelemetryPoint point);

    /**
     * Mensaje explicativo a devolver cuando esta regla es la que falla. Se usa para que el
     * error 400 le diga al cliente exactamente qué estuvo mal, no solo "payload inválido".
     */
    String violationMessage();
}
