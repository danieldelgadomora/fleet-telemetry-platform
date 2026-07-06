package com.simon.fleet.alerting.domain.model;

/**
 * Placa de un vehículo, propia del dominio de alerting-service. Se define aquí (y no se
 * comparte con ingestion-service más allá del evento en {@code contracts}) a propósito: cada
 * microservicio es dueño de su propio modelo de dominio, aunque estructuralmente se parezcan,
 * para poder evolucionar sin acoplarse entre sí. Se normaliza a mayúsculas y sin espacios
 * sobrantes en el constructor compacto, para que "abc123" y "ABC123" (o " ABC123") se
 * reconozcan como la misma placa en vez de tratarse como vehículos distintos.
 */
public record VehiclePlate(String value) {

    /** Rechaza placas vacías y normaliza a mayúsculas/sin espacios, para que placas equivalentes comparen igual. */
    public VehiclePlate {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("la placa no puede estar vacía");
        }
        value = value.trim().toUpperCase();
    }
}
