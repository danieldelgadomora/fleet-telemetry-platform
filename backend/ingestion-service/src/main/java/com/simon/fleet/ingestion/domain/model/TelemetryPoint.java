package com.simon.fleet.ingestion.domain.model;

import java.time.Instant;

/**
 * Una lectura GPS de un vehículo en un instante dado. Es el objeto central del dominio de
 * ingestion-service: nace en el controlador REST a partir del payload recibido, viaja por
 * las specifications de validación, el caché y el publisher de eventos.
 */
public record TelemetryPoint(VehicleId vehicleId, Coordinates coordinates, Instant recordedAt) {

    public TelemetryPoint {
        if (recordedAt == null) {
            throw new IllegalArgumentException("recordedAt no puede ser nulo");
        }
    }
}
