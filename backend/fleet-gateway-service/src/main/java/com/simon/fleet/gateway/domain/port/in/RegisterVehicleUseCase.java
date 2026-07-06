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
     * @throws VehicleAlreadyRegisteredException si el vehículo ya existe y sigue {@code ACTIVE}
     * o {@code PENDING_DELETION} (incluyendo el caso en que se auto-registró antes al reportar
     * telemetría): el alta explícita nunca disfraza un duplicado de éxito. Si la placa existe
     * pero está {@code DELETED}, en cambio, se reactiva como si fuera un alta nueva (sin
     * arrastrar datos de su ciclo de vida anterior) — mismo criterio que ya aplica cuando una
     * placa eliminada vuelve a reportar telemetría o generar una alerta.
     */
    Vehicle register(VehiclePlate plate);
}
