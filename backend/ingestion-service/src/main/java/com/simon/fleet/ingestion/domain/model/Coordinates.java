package com.simon.fleet.ingestion.domain.model;

/**
 * Par de coordenadas geográficas. Valida los rangos válidos de latitud/longitud al momento de
 * construirse, para que ningún punto inválido pueda existir como {@code Coordinates} en el
 * resto del sistema (falla rápido, en el borde de entrada, no más adelante).
 */
public record Coordinates(double lat, double lng) {

    private static final double MIN_LAT = -90.0;
    private static final double MAX_LAT = 90.0;
    private static final double MIN_LNG = -180.0;
    private static final double MAX_LNG = 180.0;

    /** Rechaza coordenadas fuera del rango geográfico válido, para que ninguna instancia de esta clase pueda representar un punto imposible. */
    public Coordinates {
        if (lat < MIN_LAT || lat > MAX_LAT) {
            throw new IllegalArgumentException("lat fuera de rango [-90, 90]: " + lat);
        }
        if (lng < MIN_LNG || lng > MAX_LNG) {
            throw new IllegalArgumentException("lng fuera de rango [-180, 180]: " + lng);
        }
    }

    /**
     * Compara solo lat/lng redondeados a 6 decimales (~11 cm de precisión), que es la
     * resolución útil para un GPS de vehículo. Se usa para decidir si dos lecturas son "la
     * misma coordenada" en la detección de duplicados y de vehículo detenido, sin que el ruido
     * de precisión de punto flotante haga que dos lecturas idénticas parezcan distintas.
     */
    public boolean isSameLocationAs(Coordinates other) {
        return round(this.lat) == round(other.lat) && round(this.lng) == round(other.lng);
    }

    private static double round(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }
}
