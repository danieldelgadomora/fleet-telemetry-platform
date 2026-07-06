package com.simon.fleet.ingestion.domain.model;

import java.time.Instant;

/**
 * Un botón de pánico activado por el conductor. A diferencia de {@code TelemetryPoint}, no
 * representa una lectura GPS periódica sino un hecho puntual: las coordenadas son opcionales
 * porque reflejan la última posición conocida en el dispositivo al momento de la activación, no
 * una medición nueva que deba validarse contra rangos geográficos.
 */
public record PanicButtonPress(VehiclePlate plate, Double lat, Double lng, String message, Instant triggeredAt) {

    /** Exige el momento de activación: sin él no hay forma de ordenar ni deduplicar el evento. */
    public PanicButtonPress {
        if (triggeredAt == null) {
            throw new IllegalArgumentException("triggeredAt no puede ser nulo");
        }
    }
}
