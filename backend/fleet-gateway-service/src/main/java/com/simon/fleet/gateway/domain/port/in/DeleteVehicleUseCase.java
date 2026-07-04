package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.exception.InvalidVehicleStateException;
import com.simon.fleet.gateway.domain.exception.VehicleNotFoundException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehicleId;

/**
 * Puerto de entrada (driving): arranca la Saga de eliminación de un vehículo (orquestador).
 * Marca el vehículo como {@code PENDING_DELETION} y publica el evento que ingestion-service y
 * alerting-service ya están escuchando para limpiar su propia parte (caché/Mongo y
 * alertas/Redis respectivamente).
 */
public interface DeleteVehicleUseCase {

    /**
     * @return el vehículo ya en estado {@code PENDING_DELETION}, para que el llamador pueda
     * devolverlo sin tener que volver a consultarlo.
     * @throws VehicleNotFoundException     si el vehículo no existe.
     * @throws InvalidVehicleStateException si el vehículo no está {@code ACTIVE}.
     */
    Vehicle requestDeletion(VehicleId vehicleId);
}
