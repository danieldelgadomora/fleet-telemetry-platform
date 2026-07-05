package com.simon.fleet.ingestion.domain.model;

/**
 * Placa de un vehículo. Es un value object en vez de un {@code String} suelto para que el
 * compilador impida mezclar una placa con cualquier otro texto por error, y para centralizar
 * en un solo lugar la regla de que no puede venir vacía. Se normaliza a mayúsculas y sin
 * espacios sobrantes en el constructor compacto, para que "abc123" y "ABC123" (o " ABC123")
 * se reconozcan como la misma placa en vez de tratarse como vehículos distintos.
 */
public record VehiclePlate(String value) {

    public VehiclePlate {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("la placa no puede estar vacía");
        }
        value = value.trim().toUpperCase();
    }
}
