package com.simon.fleet.ingestion.domain.port.out;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehicleId;

import java.util.Optional;

/**
 * Puerto de salida (driven) hacia el caché de "última posición conocida" por vehículo. La
 * implementación real (Redis) vive en infrastructure; el dominio solo conoce esta interfaz.
 */
public interface TelemetryCachePort {

    /**
     * @return la última posición cacheada del vehículo, o vacío si nunca reportó o si el TTL
     * ya expiró.
     */
    Optional<TelemetryPoint> findLastKnownPosition(VehicleId vehicleId);

    /**
     * Sobrescribe la última posición conocida del vehículo (con su propio TTL corto).
     */
    void saveLastKnownPosition(TelemetryPoint point);

    /**
     * Participante de la Saga de borrado: elimina del caché toda huella del vehículo (última
     * posición + claves de deduplicación). Debe ser idempotente: llamarlo sobre un vehículo ya
     * limpio no debe fallar.
     */
    void clearVehicleCache(VehicleId vehicleId);
}
