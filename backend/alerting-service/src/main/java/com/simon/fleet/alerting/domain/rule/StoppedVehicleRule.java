package com.simon.fleet.alerting.domain.rule;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Detecta un vehículo detenido: si la coordenada no cambia (ver
 * {@code Coordinates#isSameLocationAs}) por más del umbral configurado desde la primera vez
 * que se le vio ahí, genera una alerta. El umbral se recibe por constructor (no como
 * {@code @Value}) para que esta clase no dependa de Spring y sea trivial de testear con
 * distintos valores.
 */
public class StoppedVehicleRule implements AlertRule {

    static final String RULE_CODE = "STOPPED_VEHICLE";

    private final Duration stoppedThreshold;

    public StoppedVehicleRule(Duration stoppedThreshold) {
        this.stoppedThreshold = stoppedThreshold;
    }

    @Override
    public String ruleCode() {
        return RULE_CODE;
    }

    @Override
    public AlertEvaluationResult evaluate(VehicleReading reading, Optional<VehicleTrackingState> currentState) {
        boolean sameLocationAsBefore = currentState
                .map(state -> state.coordinates().isSameLocationAs(reading.coordinates()))
                .orElse(false);

        if (!sameLocationAsBefore) {
            // Coordenada nueva (o primera lectura del vehículo): reinicia el "reloj" de detenido.
            VehicleTrackingState resetState =
                    new VehicleTrackingState(reading.plate(), reading.coordinates(), reading.recordedAt());
            return new AlertEvaluationResult(resetState, Optional.empty());
        }

        VehicleTrackingState state = currentState.orElseThrow();
        Duration stoppedFor = Duration.between(state.since(), reading.recordedAt());

        if (stoppedFor.compareTo(stoppedThreshold) < 0) {
            return new AlertEvaluationResult(state, Optional.empty());
        }

        Alert alert = Alert.builder()
                .alertId(UUID.randomUUID().toString())
                .plate(reading.plate())
                .ruleCode(RULE_CODE)
                .message("Vehículo %s detenido en la misma posición desde hace %s"
                        .formatted(reading.plate().value(), formatDuration(stoppedFor)))
                .raisedAt(reading.recordedAt())
                .build();

        return new AlertEvaluationResult(state, Optional.of(alert));
    }

    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder formatted = new StringBuilder();
        if (hours > 0) {
            formatted.append(hours).append(hours == 1 ? " hora " : " horas ");
        }
        if (hours > 0 || minutes > 0) {
            formatted.append(minutes).append(minutes == 1 ? " minuto " : " minutos ");
        }
        formatted.append(seconds).append(seconds == 1 ? " segundo" : " segundos");
        return formatted.toString();
    }
}
