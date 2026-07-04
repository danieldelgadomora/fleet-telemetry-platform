package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.VehicleId;

import java.time.Instant;

/**
 * Puerto de entrada (driving): actualiza la vista de lectura del dashboard cuando
 * alerting-service publica una alerta en {@code fleet.alerts}. El vehículo queda en
 * {@code ALERTA} hasta que vuelva a reportar una coordenada distinta.
 */
public interface HandleVehicleAlertRaisedUseCase {

    /** Idempotente: aplicar la misma alerta dos veces deja el vehículo igual de en alerta. */
    void onAlertRaised(VehicleId vehicleId, Instant raisedAt);
}
