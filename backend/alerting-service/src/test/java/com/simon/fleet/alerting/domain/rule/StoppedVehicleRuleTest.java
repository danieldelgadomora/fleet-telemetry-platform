package com.simon.fleet.alerting.domain.rule;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.Coordinates;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StoppedVehicleRuleTest {

    private static final Instant BASE_TIME = Instant.parse("2026-07-05T12:00:00Z");

    private final StoppedVehicleRule rule = new StoppedVehicleRule(Duration.ofMinutes(1));
    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final Coordinates coordinates = new Coordinates(4.6, -74.08);

    @Test
    @DisplayName("la primera lectura de un vehículo nunca genera alerta y arranca su tracking")
    void primeraLecturaNoGeneraAlerta() {
        VehicleReading reading = new VehicleReading(plate, coordinates, BASE_TIME);

        AlertEvaluationResult result = rule.evaluate(reading, Optional.empty());

        assertThat(result.alert()).isEmpty();
        assertThat(result.updatedState().coordinates()).isEqualTo(coordinates);
        assertThat(result.updatedState().since()).isEqualTo(BASE_TIME);
    }

    @Test
    @DisplayName("una coordenada distinta reinicia el conteo, sin importar cuánto llevaba detenido antes")
    void coordenadaDistintaReiniciaElConteo() {
        VehicleTrackingState estadoPrevio =
                new VehicleTrackingState(plate, new Coordinates(4.0, -74.0), BASE_TIME.minus(Duration.ofHours(2)));
        VehicleReading reading = new VehicleReading(plate, coordinates, BASE_TIME);

        AlertEvaluationResult result = rule.evaluate(reading, Optional.of(estadoPrevio));

        assertThat(result.alert()).isEmpty();
        assertThat(result.updatedState().coordinates()).isEqualTo(coordinates);
        assertThat(result.updatedState().since()).isEqualTo(BASE_TIME);
    }

    @Test
    @DisplayName("si el umbral todavía no se alcanza, no genera alerta y conserva el estado")
    void umbralNoAlcanzadoNoGeneraAlerta() {
        VehicleTrackingState estadoPrevio = new VehicleTrackingState(plate, coordinates, BASE_TIME);
        VehicleReading reading = new VehicleReading(plate, coordinates, BASE_TIME.plusSeconds(59));

        AlertEvaluationResult result = rule.evaluate(reading, Optional.of(estadoPrevio));

        assertThat(result.alert()).isEmpty();
        assertThat(result.updatedState()).isEqualTo(estadoPrevio);
    }

    @Test
    @DisplayName("si el tiempo detenido es exactamente igual al umbral, sí genera alerta (borde inclusive)")
    void umbralExactoGeneraAlerta() {
        VehicleTrackingState estadoPrevio = new VehicleTrackingState(plate, coordinates, BASE_TIME);
        VehicleReading reading = new VehicleReading(plate, coordinates, BASE_TIME.plusSeconds(60));

        AlertEvaluationResult result = rule.evaluate(reading, Optional.of(estadoPrevio));

        assertThat(result.alert()).isPresent();
    }

    @Test
    @DisplayName("si el umbral se supera, genera una alerta STOPPED_VEHICLE preservando el estado original")
    void umbralSuperadoGeneraAlertaYPreservaElEstado() {
        VehicleTrackingState estadoPrevio = new VehicleTrackingState(plate, coordinates, BASE_TIME);
        VehicleReading reading = new VehicleReading(plate, coordinates, BASE_TIME.plusSeconds(125));

        AlertEvaluationResult result = rule.evaluate(reading, Optional.of(estadoPrevio));

        assertThat(result.alert()).isPresent();
        Alert alert = result.alert().get();
        assertThat(alert.getRuleCode()).isEqualTo(StoppedVehicleRule.RULE_CODE);
        assertThat(alert.getPlate()).isEqualTo(plate);
        assertThat(alert.getRaisedAt()).isEqualTo(reading.recordedAt());
        assertThat(alert.getMessage()).contains("ABC123").contains("2 minutos");
        assertThat(result.updatedState()).isEqualTo(estadoPrevio);
    }
}
