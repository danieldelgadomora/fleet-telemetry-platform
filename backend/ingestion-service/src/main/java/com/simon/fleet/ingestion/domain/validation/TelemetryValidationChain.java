package com.simon.fleet.ingestion.domain.validation;

import com.simon.fleet.ingestion.domain.model.InvalidTelemetryPayloadException;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Chain of Responsibility sobre las {@link TelemetrySpecification} registradas: recorre la
 * lista en orden y, en la primera que no se cumpla, corta la cadena y lanza
 * {@link InvalidTelemetryPayloadException} con el mensaje de esa regla específica. Si todas
 * pasan, el punto queda validado.
 */
@RequiredArgsConstructor
public class TelemetryValidationChain {

    private final List<TelemetrySpecification> specifications;

    /**
     * @throws InvalidTelemetryPayloadException en la primera regla de negocio que el punto
     *                                           incumpla.
     */
    public void validate(TelemetryPoint point) {
        for (TelemetrySpecification specification : specifications) {
            if (!specification.isSatisfiedBy(point)) {
                throw new InvalidTelemetryPayloadException(specification.violationMessage());
            }
        }
    }
}
