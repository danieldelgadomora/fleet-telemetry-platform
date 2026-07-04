package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehicleId;

/**
 * Puerto de entrada (driving): da de alta un vehículo en el registro de fleet-gateway-service.
 * Es el paso previo necesario para poder pedir su borrado después: la Saga necesita un
 * vehículo {@code ACTIVE} del cual partir.
 */
public interface RegisterVehicleUseCase {

    /**
     * Idempotente: si el vehículo ya existe, simplemente devuelve el registro existente en vez
     * de fallar.
     */
    Vehicle register(VehicleId vehicleId);
}
