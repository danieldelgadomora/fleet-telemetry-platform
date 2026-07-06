package com.simon.fleet.alerting.domain.port.in;

import com.simon.fleet.alerting.domain.model.VehiclePlate;

import java.time.Instant;

/**
 * Puerto de entrada (driving): genera la alerta correspondiente a la activación del botón de
 * pánico de un conductor. A diferencia de {@code EvaluateTelemetryUseCase}, no recorre ninguna
 * {@code AlertRule}: un botón de pánico siempre genera una alerta, no hay nada que evaluar.
 */
public interface RaisePanicAlertUseCase {

    /** Genera la alerta {@code PANIC_BUTTON} para la placa, con posición/nota opcionales. */
    void raise(VehiclePlate plate, Double lat, Double lng, String driverMessage, Instant triggeredAt);
}
