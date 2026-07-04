package com.simon.fleet.alerting.domain.rule;

import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;

import java.util.Optional;

/**
 * Strategy Pattern: cada regla de alerta implementa esta interfaz y se registra en
 * {@code EvaluateTelemetryService} (inyectada como {@code List<AlertRule>} por Spring). Agregar
 * una regla nueva (geocercas, exceso de velocidad, etc.) no requiere tocar el motor que las
 * ejecuta, solo añadir otra implementación.
 */
public interface AlertRule {

    /** Código estable de la regla (ej. "STOPPED_VEHICLE"), va en el evento publicado. */
    String ruleCode();

    /**
     * @param reading      la lectura GPS que se acaba de recibir.
     * @param currentState el último estado de tracking conocido del vehículo, o vacío si es su
     *                      primera lectura.
     * @return el estado actualizado y, si corresponde, la alerta disparada.
     */
    AlertEvaluationResult evaluate(VehicleReading reading, Optional<VehicleTrackingState> currentState);
}
