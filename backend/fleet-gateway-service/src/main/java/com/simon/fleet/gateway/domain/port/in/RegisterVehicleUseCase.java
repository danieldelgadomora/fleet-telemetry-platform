package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.exception.VehicleAlreadyRegisteredException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;

/**
 * Puerto de entrada (driving): da de alta un vehículo en el registro de fleet-gateway-service.
 * Es el paso previo necesario para poder pedir su borrado después: la Saga necesita un
 * vehículo {@code ACTIVE} del cual partir.
 */
public interface RegisterVehicleUseCase {

    /**
     * @throws VehicleAlreadyRegisteredException si el vehículo ya existe en el registro
     * (incluyendo el caso en que ya existía por haberse auto-registrado antes al reportar
     * telemetría): el alta explícita nunca disfraza un duplicado de éxito.
     */
    Vehicle register(VehiclePlate plate);
}
