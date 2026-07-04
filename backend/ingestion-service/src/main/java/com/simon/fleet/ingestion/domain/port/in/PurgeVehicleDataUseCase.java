package com.simon.fleet.ingestion.domain.port.in;

import com.simon.fleet.ingestion.domain.model.VehicleId;

/**
 * Puerto de entrada (driving): participante de la Saga coreografiada de borrado de vehículo
 * (ver README, sección "Transacciones y Consistencia"). Al recibir
 * {@code VehicleDeletionRequestedEvent}, este servicio limpia todo lo que le pertenece (caché
 * Redis + histórico Mongo) y publica su propia confirmación. No conoce ni le importa quién
 * más participa en la Saga.
 */
public interface PurgeVehicleDataUseCase {

    /**
     * Idempotente: puede llamarse más de una vez para el mismo vehículo (por reintentos de
     * RabbitMQ) sin efectos secundarios distintos a la primera vez.
     */
    void purgeVehicle(VehicleId vehicleId);
}
