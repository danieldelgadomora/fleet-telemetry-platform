package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.VehiclePlate;

import java.time.Instant;

/**
 * Puerto de entrada (driving): procesa la confirmación de ingestion-service (participante de
 * la Saga): ya limpió Redis y MongoDB de este vehículo. Si la otra confirmación
 * ({@link HandleDataPurgedUseCase}) ya había llegado antes, esta es la que completa la
 * transición a {@code DELETED}.
 */
public interface HandleCacheClearedUseCase {

    /** Idempotente: seguro de llamar más de una vez para el mismo vehículo. */
    void onCacheCleared(VehiclePlate plate, Instant clearedAt);
}
