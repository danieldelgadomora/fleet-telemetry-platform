package com.simon.fleet.alerting.domain.model;

/**
 * Identificador de un vehículo, propio del dominio de alerting-service. Se define aquí (y no
 * se comparte con ingestion-service más allá del evento en {@code contracts}) a propósito:
 * cada microservicio es dueño de su propio modelo de dominio, aunque estructuralmente se
 * parezcan, para poder evolucionar sin acoplarse entre sí.
 */
public record VehicleId(String value) {

    public VehicleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("vehicleId no puede estar vacío");
        }
    }
}
