package com.simon.fleet.alerting.domain.model;

/**
 * Zona circular donde un vehículo detenido no representa una anomalía (ej. un parqueadero de
 * la flota): un centro y un radio en metros bastan para el caso de uso, sin necesidad de
 * modelar polígonos.
 */
public record SafeZone(Long id, String name, Coordinates center, double radiusMeters) {

    /** El punto está dentro de esta zona segura. */
    public boolean contains(Coordinates point) {
        return center.distanceMetersTo(point) <= radiusMeters;
    }
}
