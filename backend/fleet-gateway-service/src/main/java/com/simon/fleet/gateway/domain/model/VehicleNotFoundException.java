package com.simon.fleet.gateway.domain.model;

/**
 * Se lanza cuando se pide una operación sobre un vehículo que no está registrado. El
 * controlador REST la traduce a un 404.
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(VehicleId vehicleId) {
        super("Vehículo no encontrado: " + vehicleId.value());
    }
}
