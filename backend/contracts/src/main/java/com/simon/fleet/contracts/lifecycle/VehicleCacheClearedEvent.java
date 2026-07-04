package com.simon.fleet.contracts.lifecycle;

import java.time.Instant;

/**
 * Confirmación publicada por ingestion-service tras completar su parte de la Saga de borrado:
 * ya eliminó del caché Redis (última posición + claves de dedupe) y del histórico en MongoDB
 * todo lo relacionado con el vehículo. fleet-gateway-service usa esta confirmación, junto con
 * {@link VehicleDataPurgedEvent}, para marcar el vehículo como definitivamente eliminado.
 *
 * @param vehicleId vehículo cuyo caché/histórico ya fue limpiado.
 * @param clearedAt momento en que se completó la limpieza.
 */
public record VehicleCacheClearedEvent(
        String vehicleId,
        Instant clearedAt
) {
}
