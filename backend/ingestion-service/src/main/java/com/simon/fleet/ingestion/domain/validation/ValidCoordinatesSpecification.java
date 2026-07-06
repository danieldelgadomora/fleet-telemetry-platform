package com.simon.fleet.ingestion.domain.validation;

import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;

/**
 * El rango válido de lat/lng ya lo garantiza {@link Coordinates} al construirse (un
 * {@code TelemetryPoint} nunca puede tener coordenadas fuera del globo). Esta specification
 * cubre una regla de negocio distinta: rechazar (0.0, 0.0), conocido como "Null Island", que
 * en la práctica casi siempre significa que el dispositivo GPS del vehículo no logró obtener
 * una posición real y envió el valor por defecto.
 */
public class ValidCoordinatesSpecification implements TelemetrySpecification {

    private static final Coordinates NULL_ISLAND = new Coordinates(0.0, 0.0);

    /** Rechaza la lectura si su coordenada es "Null Island" (0.0, 0.0). */
    @Override
    public boolean isSatisfiedBy(TelemetryPoint point) {
        return !point.coordinates().isSameLocationAs(NULL_ISLAND);
    }

    /** Mensaje explicando por qué (0.0, 0.0) se rechaza. */
    @Override
    public String violationMessage() {
        return "Coordenadas (0.0, 0.0) no son válidas: indican que el GPS no obtuvo una posición real";
    }
}
