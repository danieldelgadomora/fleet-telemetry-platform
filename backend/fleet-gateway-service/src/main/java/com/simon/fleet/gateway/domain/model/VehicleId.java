package com.simon.fleet.gateway.domain.model;

/**
 * Identificador de un vehículo, propio del dominio de fleet-gateway-service (cada
 * microservicio es dueño de su propio modelo, ver sección de arquitectura en el README).
 */
public record VehicleId(String value) {

    public VehicleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("vehicleId no puede estar vacío");
        }
    }
}
