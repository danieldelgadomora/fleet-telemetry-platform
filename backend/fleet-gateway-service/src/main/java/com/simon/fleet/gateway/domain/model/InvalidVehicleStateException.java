package com.simon.fleet.gateway.domain.model;

/**
 * Se lanza cuando se pide una transición de estado inválida (ej. borrar un vehículo que ya
 * está {@code PENDING_DELETION} o {@code DELETED}). El controlador REST la traduce a un 409.
 */
public class InvalidVehicleStateException extends RuntimeException {

    public InvalidVehicleStateException(String message) {
        super(message);
    }
}
