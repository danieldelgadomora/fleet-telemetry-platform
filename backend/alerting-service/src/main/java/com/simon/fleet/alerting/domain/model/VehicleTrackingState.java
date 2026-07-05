package com.simon.fleet.alerting.domain.model;

import java.time.Instant;

/**
 * Recuerda, por vehículo, cuál fue la última coordenada distinta que reportó y desde cuándo
 * está ahí. Es el estado mínimo que {@code StoppedVehicleRule} necesita para decidir si un
 * vehículo lleva más de un minuto "sin moverse". Vive en Redis (namespace propio de
 * alerting-service) para sobrevivir a reinicios del servicio.
 */
public record VehicleTrackingState(VehiclePlate plate, Coordinates coordinates, Instant since) {
}
