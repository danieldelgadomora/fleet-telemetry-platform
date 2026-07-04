package com.simon.fleet.gateway.domain.model;

/**
 * Se lanza cuando se pide el alta explícita de un vehículo que ya existe en el registro —
 * incluyendo el caso en que ya existía por haberse auto-registrado antes al reportar
 * telemetría. El controlador REST la traduce a un 409.
 */
public class VehicleAlreadyRegisteredException extends RuntimeException {

    public VehicleAlreadyRegisteredException(VehicleId vehicleId) {
        super("Vehículo ya registrado: " + vehicleId.value());
    }
}
