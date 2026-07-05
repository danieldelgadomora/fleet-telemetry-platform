package com.simon.fleet.ingestion.infrastructure.cache.redis;

import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;

/**
 * Centraliza el esquema de nombres de claves de Redis que usa ingestion-service, para que la
 * clave de "última posición" y la de "dedupe" de un mismo vehículo nunca se construyan de
 * forma distinta en dos lugares del código.
 */
final class RedisKeys {

    private static final String LAST_POSITION_PREFIX = "ingestion:last-position:";
    private static final String DEDUPE_PREFIX = "ingestion:dedupe:";

    private RedisKeys() {
    }

    static String lastPosition(VehiclePlate plate) {
        return LAST_POSITION_PREFIX + plate.value();
    }

    /** Patrón para borrar, con KEYS/SCAN, todas las claves de dedupe de un vehículo. */
    static String dedupePattern(VehiclePlate plate) {
        return DEDUPE_PREFIX + plate.value() + ":*";
    }

    /**
     * @param windowBucket ventana de tiempo a la que pertenece la lectura (epochSecond /
     *                      tamaño de ventana). Dos lecturas con la misma coordenada dentro del
     *                      mismo bucket producen la misma clave, y por lo tanto colisionan en
     *                      el SETNX de deduplicación.
     */
    static String dedupe(VehiclePlate plate, Coordinates coordinates, long windowBucket) {
        return DEDUPE_PREFIX + plate.value() + ":" + coordinates.lat() + ":" + coordinates.lng() + ":" + windowBucket;
    }
}
