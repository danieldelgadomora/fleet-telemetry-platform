package com.simon.fleet.alerting.domain.model;

/**
 * Par de coordenadas geográficas. {@code isSameLocationAs} redondea a 6 decimales
 * (~11 cm de precisión) para decidir si dos lecturas representan "el mismo punto" para
 * efectos de {@code StoppedVehicleRule}, sin que el ruido de punto flotante haga que dos
 * lecturas idénticas parezcan distintas.
 */
public record Coordinates(double lat, double lng) {

    public boolean isSameLocationAs(Coordinates other) {
        return round(this.lat) == round(other.lat) && round(this.lng) == round(other.lng);
    }

    private static double round(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }
}
