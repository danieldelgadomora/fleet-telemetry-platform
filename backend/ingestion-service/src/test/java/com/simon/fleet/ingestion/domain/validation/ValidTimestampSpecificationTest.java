package com.simon.fleet.ingestion.domain.validation;

import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ValidTimestampSpecificationTest {

    private static final Instant NOW = Instant.parse("2026-07-05T12:00:00Z");
    private static final long ONE_MINUTE_SECONDS = 60L;
    private static final long TWENTY_FOUR_HOURS_SECONDS = 24 * 3600L;

    private final ValidTimestampSpecification specification =
            new ValidTimestampSpecification(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("un timestamp igual al momento actual del servidor satisface la especificación")
    void timestampActualSatisfaceLaEspecificacion() {
        assertThat(specification.isSatisfiedBy(pointAt(NOW))).isTrue();
    }

    @Test
    @DisplayName("un timestamp exactamente 1 minuto en el futuro (límite inclusivo) satisface la especificación")
    void limiteExactoFuturoSatisfaceLaEspecificacion() {
        assertThat(specification.isSatisfiedBy(pointAt(NOW.plusSeconds(ONE_MINUTE_SECONDS)))).isTrue();
    }

    @Test
    @DisplayName("un timestamp un segundo más allá del límite futuro no satisface la especificación")
    void pasadoElLimiteFuturoNoSatisface() {
        assertThat(specification.isSatisfiedBy(pointAt(NOW.plusSeconds(ONE_MINUTE_SECONDS + 1)))).isFalse();
    }

    @Test
    @DisplayName("un timestamp exactamente 24 horas en el pasado (límite inclusivo) satisface la especificación")
    void limiteExactoPasadoSatisfaceLaEspecificacion() {
        assertThat(specification.isSatisfiedBy(pointAt(NOW.minusSeconds(TWENTY_FOUR_HOURS_SECONDS)))).isTrue();
    }

    @Test
    @DisplayName("un timestamp un segundo más allá del límite de antigüedad no satisface la especificación")
    void pasadoElLimiteDeAntiguedadNoSatisface() {
        assertThat(specification.isSatisfiedBy(pointAt(NOW.minusSeconds(TWENTY_FOUR_HOURS_SECONDS + 1)))).isFalse();
    }

    private static TelemetryPoint pointAt(Instant recordedAt) {
        return new TelemetryPoint(new VehiclePlate("ABC123"), new Coordinates(4.6, -74.08), recordedAt);
    }
}
