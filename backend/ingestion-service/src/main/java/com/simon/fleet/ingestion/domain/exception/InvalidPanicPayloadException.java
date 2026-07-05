package com.simon.fleet.ingestion.domain.exception;

/**
 * Se lanza cuando el payload de una activación del botón de pánico no cumple sus reglas mínimas
 * (placa vacía o timestamp no parseable). El controlador REST la traduce a un 400 Bad Request.
 */
public class InvalidPanicPayloadException extends RuntimeException {

    public InvalidPanicPayloadException(String message) {
        super(message);
    }
}
