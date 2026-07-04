package com.simon.fleet.ingestion.domain.validation;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Rechaza timestamps que no tienen sentido frente al reloj del servidor: demasiado en el
 * futuro (reloj del dispositivo desincronizado) o demasiado en el pasado (lectura obsoleta que
 * ya no describe la posición actual del vehículo). El {@link Clock} se inyecta en vez de usar
 * {@code Instant.now()} directamente para que esta regla sea determinista en tests.
 */
@RequiredArgsConstructor
public class ValidTimestampSpecification implements TelemetrySpecification {

    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(1);
    private static final Duration MAX_PAST_AGE = Duration.ofHours(24);

    private final Clock clock;

    @Override
    public boolean isSatisfiedBy(TelemetryPoint point) {
        Instant now = Instant.now(clock);
        Instant recordedAt = point.recordedAt();
        return !recordedAt.isAfter(now.plus(MAX_FUTURE_SKEW))
                && !recordedAt.isBefore(now.minus(MAX_PAST_AGE));
    }

    @Override
    public String violationMessage() {
        return "timestamp debe estar entre %s antes y %s después del momento actual del servidor"
                .formatted(MAX_PAST_AGE, MAX_FUTURE_SKEW);
    }
}
