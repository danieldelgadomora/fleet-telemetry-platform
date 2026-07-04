package com.simon.fleet.alerting.domain.rule;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;

import java.util.Optional;

/**
 * Resultado de evaluar una {@code AlertRule}: el nuevo estado de tracking a guardar (siempre
 * hay uno, incluso si no se disparó ninguna alerta) y, opcionalmente, la alerta generada.
 */
public record AlertEvaluationResult(VehicleTrackingState updatedState, Optional<Alert> alert) {
}
