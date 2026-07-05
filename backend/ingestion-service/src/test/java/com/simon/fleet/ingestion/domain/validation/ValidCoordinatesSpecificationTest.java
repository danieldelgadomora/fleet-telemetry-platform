package com.simon.fleet.ingestion.domain.validation;

import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ValidCoordinatesSpecificationTest {

    private final ValidCoordinatesSpecification specification = new ValidCoordinatesSpecification();

    @Test
    @DisplayName("una coordenada real (Bogotá) satisface la especificación")
    void coordenadaValidaSatisfaceLaEspecificacion() {
        TelemetryPoint point = telemetryPointAt(4.6, -74.08);

        assertThat(specification.isSatisfiedBy(point)).isTrue();
    }

    @Test
    @DisplayName("la coordenada exacta (0.0, 0.0) no satisface la especificación")
    void nullIslandNoSatisfaceLaEspecificacion() {
        TelemetryPoint point = telemetryPointAt(0.0, 0.0);

        assertThat(specification.isSatisfiedBy(point)).isFalse();
        assertThat(specification.violationMessage()).contains("0.0, 0.0");
    }

    @Test
    @DisplayName("una coordenada que redondea a (0.0, 0.0) en el séptimo decimal tampoco satisface la especificación")
    void coordenadaQueRedondeaANullIslandTampocoSatisface() {
        TelemetryPoint point = telemetryPointAt(0.0000001, 0.0000001);

        assertThat(specification.isSatisfiedBy(point)).isFalse();
    }

    private static TelemetryPoint telemetryPointAt(double lat, double lng) {
        return new TelemetryPoint(new VehiclePlate("ABC123"), new Coordinates(lat, lng), Instant.now());
    }
}
