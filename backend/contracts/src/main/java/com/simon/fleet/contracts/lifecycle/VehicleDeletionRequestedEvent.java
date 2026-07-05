package com.simon.fleet.contracts.lifecycle;

import java.time.Instant;

/**
 * Evento que arranca la Saga coreografiada de borrado de un vehículo. Lo publica
 * fleet-gateway-service al marcar el vehículo como {@code PENDING_DELETION}; ingestion-service
 * y alerting-service lo consumen para limpiar cada uno su propio almacén de datos del
 * vehículo, confirmando con su propio evento de finalización ({@link VehicleCacheClearedEvent},
 * {@link VehicleDataPurgedEvent}).
 *
 * @param plate       placa del vehículo a eliminar.
 * @param requestedAt momento en que se solicitó el borrado.
 */
public record VehicleDeletionRequestedEvent(
        String plate,
        Instant requestedAt
) {
}
