package com.simon.fleet.alerting.domain.port.in;

import com.simon.fleet.alerting.domain.model.VehiclePlate;

/**
 * Puerto de entrada (driving): participante de la Saga coreografiada de borrado de vehículo,
 * del lado de alerting-service: purga las alertas y el estado de tracking del vehículo, y
 * confirma con su propio evento.
 */
public interface PurgeVehicleAlertsUseCase {

    /** Idempotente: puede llamarse más de una vez para el mismo vehículo sin efectos extra. */
    void purge(VehiclePlate plate);
}
