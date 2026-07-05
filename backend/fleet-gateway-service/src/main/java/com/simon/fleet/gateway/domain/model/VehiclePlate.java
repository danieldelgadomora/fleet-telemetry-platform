package com.simon.fleet.gateway.domain.model;

/**
 * Placa de un vehículo, propia del dominio de fleet-gateway-service (cada microservicio es
 * dueño de su propio modelo, ver sección de arquitectura en el README). Se normaliza a
 * mayúsculas y sin espacios sobrantes en el constructor compacto, para que "abc123" y
 * "ABC123" (o " ABC123") se reconozcan como la misma placa en vez de tratarse como vehículos
 * distintos.
 */
public record VehiclePlate(String value) {

    public VehiclePlate {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("la placa no puede estar vacía");
        }
        value = value.trim().toUpperCase();
    }
}
