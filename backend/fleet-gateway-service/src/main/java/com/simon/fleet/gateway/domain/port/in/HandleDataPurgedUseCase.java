package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.VehiclePlate;

import java.time.Instant;

/**
 * Puerto de entrada (driving): procesa la confirmación de alerting-service (participante de la
 * Saga): ya purgó las alertas en PostgreSQL de este vehículo. Ver
 * {@link HandleCacheClearedUseCase} para el otro lado.
 */
public interface HandleDataPurgedUseCase {

    /** Idempotente: seguro de llamar más de una vez para el mismo vehículo. */
    void onDataPurged(VehiclePlate plate, Instant purgedAt);
}
