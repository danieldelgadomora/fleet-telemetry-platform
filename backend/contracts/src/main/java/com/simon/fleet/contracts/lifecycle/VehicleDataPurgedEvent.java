package com.simon.fleet.contracts.lifecycle;

import java.time.Instant;

/**
 * Confirmación publicada por alerting-service tras completar su parte de la Saga de borrado:
 * ya purgó de PostgreSQL todas las alertas asociadas al vehículo. Junto con
 * {@link VehicleCacheClearedEvent}, es la señal que fleet-gateway-service necesita para marcar
 * el vehículo como definitivamente eliminado.
 *
 * @param plate    placa del vehículo cuyas alertas ya fueron purgadas.
 * @param purgedAt momento en que se completó la purga.
 */
public record VehicleDataPurgedEvent(
        String plate,
        Instant purgedAt
) {
}
