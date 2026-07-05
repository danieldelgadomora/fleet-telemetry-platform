package com.simon.fleet.gateway.domain.exception;

import com.simon.fleet.gateway.domain.model.VehiclePlate;

/**
 * Se lanza cuando se pide una operación sobre un vehículo que no está registrado. El
 * controlador REST la traduce a un 404.
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(VehiclePlate plate) {
        super("Vehículo no encontrado: " + plate.value());
    }
}
