package com.simon.fleet.ingestion.domain.port.out;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;

/**
 * Puerto de salida (driven) para el mecanismo anti-duplicados. La implementación real (Redis,
 * con SETNX y un TTL corto sobre una clave derivada del vehículo + coordenada + ventana de
 * tiempo) vive en infrastructure.
 */
public interface TelemetryDeduplicationPort {

    /**
     * Marca la lectura como "vista" para su ventana de tiempo y responde si ya existía una
     * lectura idéntica (mismo vehículo + misma coordenada) dentro de esa misma ventana. Es una
     * operación atómica: dos lecturas idénticas que lleguen casi al mismo tiempo nunca deberían
     * pasar ambas como "no duplicada".
     *
     * @return true si esta lectura es un duplicado y debe ignorarse.
     */
    boolean isDuplicate(TelemetryPoint point);
}
