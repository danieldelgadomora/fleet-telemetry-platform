package com.simon.fleet.alerting.domain.rule;

import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;
import com.simon.fleet.alerting.domain.port.out.GeofenceRepositoryPort;

import java.util.Optional;

/**
 * Decora otra {@code AlertRule} para suprimir sus alertas cuando el vehículo se encuentra
 * dentro de una zona segura activa (ej. un parqueadero): un vehículo detenido dentro de una
 * geocerca conocida no es una situación anómala, así que no debe generar alerta, aunque la
 * regla decorada sí la habría disparado. El estado de tracking que calculó la regla decorada se
 * conserva sin cambios: esta clase solo decide si la alerta se propaga o se descarta, nunca
 * recalcula el estado.
 */
public class SafeZoneAwareAlertRule implements AlertRule {

    private final AlertRule delegate;
    private final GeofenceRepositoryPort geofenceRepositoryPort;

    public SafeZoneAwareAlertRule(AlertRule delegate, GeofenceRepositoryPort geofenceRepositoryPort) {
        this.delegate = delegate;
        this.geofenceRepositoryPort = geofenceRepositoryPort;
    }

    @Override
    public String ruleCode() {
        return delegate.ruleCode();
    }

    @Override
    public AlertEvaluationResult evaluate(VehicleReading reading, Optional<VehicleTrackingState> currentState) {
        AlertEvaluationResult result = delegate.evaluate(reading, currentState);

        if (result.alert().isEmpty()) {
            return result;
        }

        boolean dentroDeZonaSegura = geofenceRepositoryPort.findAllActive().stream()
                .anyMatch(zone -> zone.contains(reading.coordinates()));

        if (dentroDeZonaSegura) {
            return new AlertEvaluationResult(result.updatedState(), Optional.empty());
        }

        return result;
    }
}
