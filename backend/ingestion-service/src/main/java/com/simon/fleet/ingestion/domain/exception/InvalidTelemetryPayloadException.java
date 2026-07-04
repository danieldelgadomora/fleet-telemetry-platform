package com.simon.fleet.ingestion.domain.exception;

/**
 * Se lanza cuando un payload de telemetría no cumple alguna de las reglas de negocio de la
 * {@code TelemetryValidationChain} (coordenadas fuera de rango, timestamp inválido, etc.).
 * El controlador REST la traduce a un 400 Bad Request.
 */
public class InvalidTelemetryPayloadException extends RuntimeException {

    public InvalidTelemetryPayloadException(String message) {
        super(message);
    }
}
