package com.simon.fleet.ingestion.domain.model;

/**
 * Identificador de un vehículo. Es un value object en vez de un {@code String} suelto para
 * que el compilador impida mezclar un vehicleId con cualquier otro texto por error, y para
 * centralizar en un solo lugar la regla de que no puede venir vacío.
 */
public record VehicleId(String value) {

    public VehicleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("vehicleId no puede estar vacio");
        }
    }
}
