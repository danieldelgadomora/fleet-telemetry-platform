package com.simon.fleet.alerting.domain.model;

/**
 * Par de coordenadas geográficas. {@code isSameLocationAs} redondea a 6 decimales
 * (~11 cm de precisión) para decidir si dos lecturas representan "el mismo punto" para
 * efectos de {@code StoppedVehicleRule}, sin que el ruido de punto flotante haga que dos
 * lecturas idénticas parezcan distintas.
 */
public record Coordinates(double lat, double lng) {

    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    /** Compara ambas coordenadas redondeadas a 6 decimales, para decidir si representan el mismo punto. */
    public boolean isSameLocationAs(Coordinates other) {
        return round(this.lat) == round(other.lat) && round(this.lng) == round(other.lng);
    }

    /**
     * Distancia real entre dos puntos sobre la superficie terrestre (fórmula de Haversine),
     * en metros. A diferencia de {@code isSameLocationAs} (que solo decide si dos lecturas son
     * "el mismo punto"), esto se usa para saber si un punto cae dentro del radio de una zona
     * segura.
     */
    public double distanceMetersTo(Coordinates other) {
        double lat1 = Math.toRadians(this.lat);
        double lat2 = Math.toRadians(other.lat);
        double deltaLat = Math.toRadians(other.lat - this.lat);
        double deltaLng = Math.toRadians(other.lng - this.lng);

        double haversineOfCentralAngle = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double angularDistanceRadians = 2 * Math.atan2(
                Math.sqrt(haversineOfCentralAngle), Math.sqrt(1 - haversineOfCentralAngle));

        return EARTH_RADIUS_METERS * angularDistanceRadians;
    }

    private static double round(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }
}
